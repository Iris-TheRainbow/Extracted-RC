/*
Copyright (c) 2017 Craig MacFarlane, Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Craig MacFarlane, Robert Atkinson nor the names of his
contributors may be used to endorse or promote products derived from this software
without specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.qualcomm.hardware.lynx;

import android.content.Context;

import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadMultipleBytesCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadSingleByteCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteSingleByteCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.TimestampedI2cData;

/*
 * Used for firmware versions prior to 1.8.2
 */
public class LynxI2cDeviceSynchV1 extends LynxI2cDeviceSynch
{
    public LynxI2cDeviceSynchV1(Context context, LynxModule module, int bus) {
        super(context, module, bus);
    }

    @Override
    public synchronized TimestampedData readTimeStamped(final int ireg, final int creg)
    {
        try {
            final Supplier<LynxI2cWriteSingleByteCommand> writeTxSupplier = new Supplier<LynxI2cWriteSingleByteCommand>()
                {
                @Override
                public LynxI2cWriteSingleByteCommand get()
                    {
                    return new LynxI2cWriteSingleByteCommand(getModule(), bus, i2cAddr, ireg);
                    }
                };

            final Supplier<LynxCommand<?>> readTxSupplier = new Supplier<LynxCommand<?>>()
                {
                @Override
                public LynxCommand<?> get()
                    {
                    /*
                     * LynxI2cReadMultipleBytesCommand does not support a
                     * byte count of one, so we manually differentiate here.
                     */
                    return creg==1
                            ? new LynxI2cReadSingleByteCommand(getModule(), bus, i2cAddr)
                            : new LynxI2cReadMultipleBytesCommand(getModule(), bus, i2cAddr, creg);
                    }
                };

            return acquireI2cLockWhile(new Supplier<TimestampedData>()
            {
                @Override public TimestampedData get() throws InterruptedException, RobotCoreException, LynxNackException
                {
                    sendI2cTransaction(writeTxSupplier);
                    internalWaitForWriteCompletions(I2cWaitControl.ATOMIC);
                    sendI2cTransaction(readTxSupplier);

                    readTimeStampedPlaceholder.reset();
                    return pollForReadResult(i2cAddr, ireg, creg);
                }
            });
        } catch (InterruptedException|RobotCoreException|RuntimeException e) {
            handleException(e);
        } catch (LynxNackException e) {
            /*
             * This is a possible device problem, go ahead and tell I2cWarningManager to warn.
             */
            I2cWarningManager.notifyProblemI2cDevice(this);
            handleException(e);
        }
        return readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), ireg, creg));
    }
}

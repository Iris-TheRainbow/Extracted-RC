/*
Copyright (c) 2018 Noah Andrews

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Noah Andrews nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

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
package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager.ClassSource;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ServoConfigurationType} contains the meta-data for a user-defined servo type.
 */
public final class ServoConfigurationType extends InstantiableUserConfigurationType {

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------
    private static final ConstructorPrototype ctorServo = new ConstructorPrototype(ServoController.class, int.class);
    private static final ConstructorPrototype ctorServoEx = new ConstructorPrototype(ServoControllerEx.class, int.class);

    // Maybe someday we'll expose servoFlavor via the config UI, but the other information does not
    // seem very useful to the user.
    private @Expose ServoFlavor servoFlavor;
    private double usPulseLower;
    private double usPulseUpper;
    private double usFrame;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    public ServoConfigurationType(Class<? extends HardwareDevice> clazz, String xmlTag, ClassSource classSource) {
        super(clazz, DeviceFlavor.SERVO, xmlTag, new ConstructorPrototype[]{ctorServo, ctorServoEx}, classSource);
    }

    // Used by gson deserialization
    @SuppressWarnings("unused")
    public ServoConfigurationType() {
        super(DeviceFlavor.SERVO);
    }

    public static ServoConfigurationType getStandardServoType() {
        return ConfigurationTypeManager.getInstance().getStandardServoType();
    }

    public void processAnnotation(@Nullable ServoType servoType) {
        if (servoType != null) {
            servoFlavor = servoType.flavor();
            usPulseLower = servoType.usPulseLower();
            usPulseUpper = servoType.usPulseUpper();
            usFrame = servoType.usPulseFrameRate();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Accessing
    //----------------------------------------------------------------------------------------------

    public ServoFlavor getServoFlavor() {
        return servoFlavor;
    }

    public double getUsPulseLower() {
        return usPulseLower;
    }

    public double getUsPulseUpper() {
        return usPulseUpper;
    }

    public double getUsFrame() {
        return usFrame;
    }

    @Override
    public boolean annotatedClassIsInstantiable() {
        return servoFlavor == ServoFlavor.CUSTOM;
    }

    //----------------------------------------------------------------------------------------------
    // Instance creation
    //----------------------------------------------------------------------------------------------

    public List<HardwareDevice> createInstances(ServoControllerEx controller, int port) {
        if (servoFlavor != ServoFlavor.CUSTOM)
            throw new RuntimeException("Can't create instance of non-instantiable servo type " + name);

        final List<HardwareDevice> result = new ArrayList<>(additionalTypesToInstantiate.size() + 1);
        forThisAndAllAdditionalTypes(type -> {
            try {
                Constructor<HardwareDevice> ctor;

                ctor = type.findMatch(ctorServoEx);
                if (ctor != null) {
                    controller.setServoType(port, this);
                    result.add(ctor.newInstance(controller, port));
                    return; // Exits the forThisAndAllAdditionalTypes() lambda, NOT createInstances()
                }

                ctor = type.findMatch(ctorServo);
                if (ctor != null) {
                    controller.setServoType(port, this);
                    result.add(ctor.newInstance(controller, port));
                    return; // Exits the forThisAndAllAdditionalTypes() lambda, NOT createInstances()
                }

                RobotLog.e("Unable to locate constructor for device class " + type.getClazz().getName());
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                handleConstructorExceptions(e, type.getClazz());
            }
        });
        return result;
    }

    //----------------------------------------------------------------------------------------------
    // Serialization (used in local marshalling during configuration editing)
    //----------------------------------------------------------------------------------------------

    private Object writeReplace() {
        return new SerializationProxy(this);
    }
}

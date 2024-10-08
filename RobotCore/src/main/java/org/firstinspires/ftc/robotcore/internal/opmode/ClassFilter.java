/*
 * Copyright (c) 2015 Craig MacFarlane
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Craig MacFarlane nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.robotcore.internal.opmode;

/**
 * Classes that want to iterate over the list of all classes in the APK should
 * implement this interface and register themselves with the ClassManager via
 * the ClassManagerFactory.
 */
public interface ClassFilter {

    /**
     * Clears the result of any previous filtering in preparation for further filtering
     */
    void filterAllClassesStart();
    void filterOnBotJavaClassesStart();
    void filterExternalLibrariesClassesStart();

    // TODO(Noah): In a major or off-season release, replace the various filterClass() methods
    //             with one that accepts a Class<?> and a ClassSource enum (note the type param)
    //
    /**
     * Don't call me, I'll call you.
     *
     * @param clazz Look me in the mirror.
     */
    void filterClass(Class clazz);
    void filterOnBotJavaClass(Class clazz);
    void filterExternalLibrariesClass(Class clazz);

    /**
     * Called when a filtering cycle is complete
     */
    void filterAllClassesComplete();
    void filterOnBotJavaClassesComplete();
    void filterExternalLibrariesClassesComplete();
}

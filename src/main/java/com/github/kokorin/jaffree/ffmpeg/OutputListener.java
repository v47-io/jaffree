/*
 *    Copyright  2019-2021 Denis Kokorin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.kokorin.jaffree.ffmpeg;

import io.v47.jaffree.process.ProcessAccess;

/**
 * Extend this interface to analyze ffmpeg output.
 */
public interface OutputListener {
    /**
     * Invoked for every ffmpeg log message with level INFO and higher.
     * <p>
     * Attention: this method is not thread safe and may be invoked in different thread.
     * Consider using synchronization.
     *
     * @param message       ffmpeg log message
     * @param processAccess gives access to the process that produced that output
     */
    void onOutput(String message, ProcessAccess processAccess);
}

/*
 *    Copyright 2017 Denis Kokorin
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
 * Implement {@link ProgressListener} interface to receive ffmpeg progress reports.
 *
 * @see FFmpeg#setProgressListener(ProgressListener)
 */
public interface ProgressListener {
    /**
     * Attention: this method is not thread safe and may be invoked in different thread.
     * Consider using synchronization.
     *
     * @param progress      ffmpeg progress
     * @param processAccess gives access to the current ffmpeg process
     */
    void onProgress(FFmpegProgress progress, ProcessAccess processAccess);
}

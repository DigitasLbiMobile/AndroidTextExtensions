/*
 * Copyright (c) 2015 DigitasLBi.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package digitaslbi.ext;

/**
 * Created by anatriep on 21/05/2015.
 */
public abstract class Extension {

    public final static Extension FONT_EXTENSION = new Extension() {
        @Override
        public int getId() {
            return 0x01;
        }
    };
    public final static Extension DRAWABLE_EXTENSION = new Extension() {
        @Override
        public int getId() {
            return 0x02;
        }
    };

    public abstract int getId();

}

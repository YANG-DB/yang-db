package com.yangdb.logging.mdc;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.logging.slf4j.ExtendedMDC;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

public interface MDCWriter {
    void write();

    class Noop implements MDCWriter {
        public static final Noop instance = new Noop();

        //region Constructors
        private Noop() {}
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {

        }
        //endregion
    }

    class KeyValue<TValue> implements MDCWriter {
        //region Constructors
        public KeyValue(String key, TValue value) {
            this.key = key;
            this.value = value;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            if (this.value != null) {
                if (this.value instanceof String) {
                    MDC.put(this.key, (String)this.value);
                } else {
                    ExtendedMDC.put(this.key, this.value);
                }
            }
        }
        //endregion

        //region Fields
        protected String key;
        protected TValue value;
        //endregion
    }

    class Composite implements MDCWriter {
        //region Static
        public static Composite of(MDCWriter...writers) {
            return new Composite(writers);
        }
        //endregion

        //region Constructors
        public Composite(MDCWriter...writers) {
            List<MDCWriter> writerList = new ArrayList<>();
            for(MDCWriter writer : writers) {
                writerList.add(writer);
            }
            this.writers = writerList;
        }

        public Composite(Iterable<MDCWriter> writers) {
            this.writers = writers;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            for(MDCWriter writer : this.writers) {
                writer.write();
            }
        }
        //endregion

        //region Fields
        private Iterable<MDCWriter> writers;
        //endregion
    }

    class Supplier implements MDCWriter {
        //region Constructors
        public Supplier(java.util.function.Supplier<MDCWriter> mdcWriterSupplier) {
            this.mdcWriterSupplier = mdcWriterSupplier;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            MDCWriter mdcWriter = this.mdcWriterSupplier.get();
            if (mdcWriter != null) {
                mdcWriter.write();
            }
        }
        //endregion

        //region Fields
        private java.util.function.Supplier<MDCWriter> mdcWriterSupplier;
        //endregion
    }
}

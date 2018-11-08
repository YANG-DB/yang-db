package org.unipop.common.valueSuppliers;

/**
 * Created by Roman on 8/21/2018.
 */
public interface Clock {
    long getTime();

    class System implements Clock {
        public static System instance = new System();

        //region Clock Implementation
        @Override
        public long getTime() {
            return java.lang.System.currentTimeMillis();
        }
        //endregion
    }

    class Manual implements Clock {
        //region Constructors
        public Manual() {
            this(0L);
        }

        public Manual(long time) {
            this.time = time;
        }
        //endregion

        //region Clock Implementation
        @Override
        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
        //endregion

        //region Fields
        private long time;
        //endregion
    }
}

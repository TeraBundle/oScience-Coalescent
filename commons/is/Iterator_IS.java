/*
 * Copyright 2010-2014 Susanta Tewari. <statsusant@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package commons.is;

import com.google.common.base.Stopwatch;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @since 1.4.2
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on 12/18/13.
 */
abstract class Iterator_IS {

    private boolean hasNext;

    final boolean hasNext() {
        return hasNext ? hasNextImpl() : false;
    }

    protected abstract boolean hasNextImpl();

    abstract double getFractionCompleted();

    void start() {
        hasNext = true;
    }

    final void stop() {
        hasNext = false;
    }

    public static Iterator_IS of_Time(int time) {
        return new Iterator_IS_Time(time);
    }

    /**
     * @since since
     * @version version
     * @author Susanta Tewari
     * @history Created on 4/8/14.
     */
    private static class Iterator_IS_Time extends Iterator_IS {

        private final int delay;
        private final Stopwatch stopwatch;
        private final Timer timer;
        private final int time;
        private boolean stopped;
        final TimerTask task;

        public Iterator_IS_Time(int time) {

            this.time = time;
            delay     = time + 1000;
            stopwatch = new Stopwatch();
            timer     = new Timer();
            stopped   = false;
            task      = new TimerTask() {

                @Override
                public void run() {

                    stop();

                    stopped = true;

                    timer.cancel();
                    stopwatch.stop();
                }
            };
        }

        @Override
        void start() {

            super.start();
            stopwatch.start();
            timer.schedule(task, delay);    // to make up for 1s delay; SO: 20494397
        }

        @Override
        public boolean hasNextImpl() {
            return !stopped;
        }

        @Override
        double getFractionCompleted() {

            final long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);

            return (double) elapsed / delay;
        }
    }
}

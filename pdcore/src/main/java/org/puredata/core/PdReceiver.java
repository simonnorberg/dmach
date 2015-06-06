/**
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL WARRANTIES, see the
 * file, "LICENSE.txt," in this distribution.
 *
 */

package org.puredata.core;

/**
 *
 * PdReceiver is an interface for printing and receiving messages from Pd, to be used with
 * setReceiver in {@link org.puredata.core.PdBase}.
 *
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public interface PdReceiver extends PdListener {

    /**
     * Print output from Pd print objects
     *
     * @param s String to be printed
     */
    void print(String s);

    /**
     * Adapter for PdReceiver implementations that only need to handle a subset of Pd messages
     */
    class Adapter extends PdListener.Adapter implements PdReceiver {
        @Override
        public void print(String s) {}
    }
}

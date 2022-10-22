package de.mark225.beam.util;

import de.mark225.beam.data.BeamPlayer;

import java.util.Set;
import java.util.logging.Logger;

public final class BeamUtils {

        private BeamUtils() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static Logger getLogger(){
            return Logger.getLogger("BeamCore");
        }

        public static Set<BeamPlayer> getOnlinePlayers() {
            //TODO: Implement
            return null;
        }
}

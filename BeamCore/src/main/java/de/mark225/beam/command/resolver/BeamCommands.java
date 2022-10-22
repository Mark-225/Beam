package de.mark225.beam.command.resolver;

import de.bluecolored.bluemap.api.BlueMapWorld;
import de.mark225.beam.data.BeamCommandSender;

public final class BeamCommands {
    private BeamCommands() {
        throw new UnsupportedOperationException();
    }

    @RegisterBeamCommand(
        label = "bmarker",
        description = "Marker creation command",
        permission = "beam.command.bmarker.poi.create",
        arguments = {"literal/create", "literal/poi", "string", "string", "double", "double", "double", "world"},
        argumentLabels = {"create", "poi", "id", "label", "x", "y", "z", "world"}
    )
    public static String markerCreate(BeamCommandSender sender, String usedAlias, String id, String label, double x, double y, double z, BlueMapWorld world) {
        //TODO: Implement marker creation
        return "response";
    }




}

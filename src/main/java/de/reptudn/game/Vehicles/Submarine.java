package de.reptudn.game.Vehicles;

import de.reptudn.game.Crew.ACrewMember;
import net.minestom.server.entity.Player;

public class Submarine extends AVehicle {

    private Player owner;
    private ACrewMember[] crewMembers;

    private static final int MAX_CREW_MEMBERS = 5;

    public Submarine(Player owner, int new_id) {
        super(new_id);
        this.owner = owner;
    }

}

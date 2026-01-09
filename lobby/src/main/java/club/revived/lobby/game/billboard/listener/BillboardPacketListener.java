package club.revived.lobby.game.billboard.listener;

import club.revived.lobby.game.billboard.BillboardManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public final class BillboardPacketListener implements PacketListener {

    private final BillboardManager billboardManager = BillboardManager.getInstance();

    /**
     * Creates a BillboardPacketListener and registers it with the PacketEvents event manager at NORMAL priority.
     */
    public BillboardPacketListener() {
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    /**
     * Handle incoming INTERACT_ENTITY packets to trigger billboard interactions.
     *
     * Processes only Play.Client.INTERACT_ENTITY packets whose action is INTERACT or ATTACK. If the packet's entity ID
     * corresponds to a registered billboard, invokes that billboard's wrapperEntityConsumer with the billboard's entity
     * and the player who sent the packet.
     *
     * @param e the packet receive event containing the packet and the player who sent it
     */
    @Override
    public void onPacketReceive(final PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }

        final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);

        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT &&
                packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK
        ) {
            return;
        }

        final int id = packet.getEntityId();

        if (!this.billboardManager.getBillboards().containsKey(id)) {
            return;
        }

        final var billboard = this.billboardManager.getBillboards().get(id);
        billboard.wrapperEntityConsumer().accept(billboard.entity(), e.getPlayer());
    }
}
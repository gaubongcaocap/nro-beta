package event.event_manifest;

/*
 *
 *
 * @author EMTI
 */

import boss.BossID;
import event.Event;

public class HungVuong extends Event {

    @Override
    public void boss() {
        createBoss(BossID.THUY_TINH, 10);
    }

    @Override
    public void npc() {
        createNpc(7, 52, 816, 432);

    }
}

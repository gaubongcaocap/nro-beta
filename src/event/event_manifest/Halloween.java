package event.event_manifest;

/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import boss.BossID;
import event.Event;

public class Halloween extends Event {

    @Override
    public void npc() {
    }

    @Override
    public void boss() {
        createBoss(BossID.BIMA, 10);
        createBoss(BossID.MATROI, 10);
        createBoss(BossID.DOI, 10);
    }
}

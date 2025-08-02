/*
 * Copyright by EMTI
 */

package minigame;

import minigame.LuckyNumber.LuckNumberData;

import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class Test {
    public static List<LuckNumberData> listPlayer = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(new Timestamp(System.currentTimeMillis()).getTime());
    }
}

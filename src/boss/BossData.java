package boss;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

/*
 * This class defines the data configuration for a boss. Previously it was
 * annotated with Lombok's @Data annotation which generated getters,
 * setters, equals and hashCode methods automatically. Lombok is not
 * available in this build environment, so these features are provided
 * manually. Only the getters used throughout the code base are
 * implemented. Additional getters can be added as needed if new fields
 * are accessed elsewhere. Setters are intentionally omitted since the
 * fields are meant to be immutable once set via the constructors.
 */

public class BossData {

    public static final int DEFAULT_APPEAR = 0;
    public static final int APPEAR_WITH_ANOTHER = 1;
    public static final int ANOTHER_LEVEL = 2;

    private String name;

    private byte gender;

    private short[] outfit;

    private long dame;

    private long[] hp;

    private int[] mapJoin;

    private int[][] skillTemp;

    private String[] textS;

    private String[] textM;

    private String[] textE;

    private int secondsRest;

    private AppearType typeAppear;

    private int[] bossesAppearTogether;

    private BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE) {
        this.name = name;
        this.gender = gender;
        this.outfit = outfit;
        this.dame = dame;
        this.hp = hp;
        this.mapJoin = mapJoin;
        this.skillTemp = skillTemp;
        this.textS = textS;
        this.textM = textM;
        this.textE = textE;
        this.secondsRest = 0;
        this.typeAppear = AppearType.DEFAULT_APPEAR;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.secondsRest = secondsRest;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, int[] bossesAppearTogether) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.bossesAppearTogether = bossesAppearTogether;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, AppearType typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.typeAppear = typeAppear;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, AppearType typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.typeAppear = typeAppear;
    }
    /**
     * Returns the maps where this boss can join.
     * @return array of map IDs
     */
    public int[] getMapJoin() {
        return this.mapJoin;
    }

    /**
     * Returns the outfit associated with this boss.
     * @return array of outfit parts
     */
    public short[] getOutfit() {
        return this.outfit;
    }

    /**
     * Returns the display name of the boss.
     * @return name string
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the gender of the boss.
     * @return gender byte
     */
    public byte getGender() {
        return this.gender;
    }

    /**
     * Returns the base damage of the boss.
     * @return damage value
     */
    public long getDame() {
        return this.dame;
    }

    /**
     * Returns the HP pool values for each form of the boss. Typically a random
     * element is selected when spawning the boss.
     * @return array of HP values
     */
    public long[] getHp() {
        return this.hp;
    }

    /**
     * Returns the skill templates used by the boss at different levels.
     * @return two‑dimensional array representing skill templates
     */
    public int[][] getSkillTemp() {
        return this.skillTemp;
    }

    /**
     * Returns the text displayed when the boss spawns.
     * @return array of spawn messages
     */
    public String[] getTextS() {
        return this.textS;
    }

    /**
     * Returns the text displayed in the middle of the fight.
     * @return array of mid fight messages
     */
    public String[] getTextM() {
        return this.textM;
    }

    /**
     * Returns the text displayed when the boss is defeated or exits.
     * @return array of end messages
     */
    public String[] getTextE() {
        return this.textE;
    }

    /**
     * Returns the number of seconds the boss rests between appearances.
     * @return rest duration in seconds
     */
    public int getSecondsRest() {
        return this.secondsRest;
    }

    /**
     * Returns the appear type of the boss. See {@link AppearType} for
     * possible values.
     * @return appear type
     */
    public AppearType getTypeAppear() {
        return this.typeAppear;
    }

    /**
     * Returns an array of IDs for bosses that appear together with this boss.
     * May be null if no bosses appear together.
     * @return array of boss IDs or null
     */
    public int[] getBossesAppearTogether() {
        return this.bossesAppearTogether;
    }
}

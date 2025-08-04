package power;

/*
 * This class previously relied on Lombok annotations to generate
 * boilerplate such as getters, setters and a builder. Since Lombok
 * is not available in the build environment, the annotations have
 * been removed and the equivalent functionality is implemented
 * manually. A simple builder pattern is provided via the inner
 * Builder class so that existing code calling Caption.builder()
 * continues to work. Getters and setters are explicitly defined
 * below. See CaptionManager.load() for usage of the builder.
 */

/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

public class Caption {
    private int id;
    private String earth;
    private String saiya;
    private String namek;
    private long power;

    /**
     * Default no‑arg constructor.
     */
    public Caption() {
    }

    /**
     * Full constructor used by the Builder.
     */
    public Caption(int id, String earth, String saiya, String namek, long power) {
        this.id = id;
        this.earth = earth;
        this.saiya = saiya;
        this.namek = namek;
        this.power = power;
    }

    /**
     * Returns the caption text based on planet index. 0 = earth, 1 = namek, 2 = saiya.
     */
    public String getCaption(int planet) {
        String caption = earth;
        if (planet == 1) {
            caption = namek;
        } else if (planet == 2) {
            caption = saiya;
        }
        return caption;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEarth() {
        return earth;
    }

    public void setEarth(String earth) {
        this.earth = earth;
    }

    public String getSaiya() {
        return saiya;
    }

    public void setSaiya(String saiya) {
        this.saiya = saiya;
    }

    public String getNamek() {
        return namek;
    }

    public void setNamek(String namek) {
        this.namek = namek;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    /**
     * Builder for Caption objects. Supports chained setter methods to create
     * an instance of Caption. Example usage:
     * <pre>
     * Caption caption = Caption.builder()
     *     .id(1)
     *     .earth("Earth text")
     *     .saiya("Saiya text")
     *     .namek("Namek text")
     *     .power(1000L)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private int id;
        private String earth;
        private String saiya;
        private String namek;
        private long power;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder earth(String earth) {
            this.earth = earth;
            return this;
        }

        public Builder saiya(String saiya) {
            this.saiya = saiya;
            return this;
        }

        public Builder namek(String namek) {
            this.namek = namek;
            return this;
        }

        public Builder power(long power) {
            this.power = power;
            return this;
        }

        public Caption build() {
            return new Caption(id, earth, saiya, namek, power);
        }
    }

    /**
     * Static entry point for the builder. Allows code to call
     * Caption.builder() to begin constructing a new Caption instance.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}

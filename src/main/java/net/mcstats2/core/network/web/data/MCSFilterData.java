package net.mcstats2.core.network.web.data;

public class MCSFilterData {
    private FilterDescription description;
    private MatchData[] matches;

    public FilterDescription getDescription() {
        return description;
    }

    public MatchData[] getMatchData() {
        return matches;
    }

    public class FilterDescription {
        private String id;
        private String name;
        private String version;
        private FilterType type;
        private String author;
        private String[] authors;
        private String website;

        public String getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public FilterType getType() {
            return type;
        }

        public String getAuthor() {
            return author;
        }

        public String[] getAuthors() {
            return authors;
        }

        public String getWebsite() {
            return website;
        }
    }

    public class MatchData {
        private String id;
        private String name;
        private MatchType type;
        private String match;
        private int score;

        public String getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public MatchType getMatchType() {
            return type;
        }

        public String getMatch() {
            return match;
        }

        public int getScore() {
            return score;
        }
    }

    public enum FilterType {
        ADVERSE,
        BADWORDS,
        TEXT_NORMALIZER
    }

    public enum MatchType  {
        REGEX,
        TEXT
    }
}

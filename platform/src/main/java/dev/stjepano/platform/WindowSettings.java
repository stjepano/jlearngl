package dev.stjepano.platform;

public record WindowSettings(int width, int height, String title, boolean vsync) {


    public static WindowSettings.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int width;
        private int height;
        private String title;
        private boolean vsync;

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder vsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public WindowSettings build() {
            return new WindowSettings(width, height, title, vsync);
        }

    }
}

package com.development.nest.time.wrap.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavArgs;

import java.io.Serializable;
import java.util.HashMap;

public class SaveFileFragmentArgs implements NavArgs {
    private final HashMap arguments = new HashMap();

    private SaveFileFragmentArgs() {
    }

    @SuppressWarnings("unchecked")
    private SaveFileFragmentArgs(HashMap argumentsMap) {
        this.arguments.putAll(argumentsMap);
    }

    @NonNull
    @SuppressWarnings({
            "unchecked",
            "deprecation"
    })
    public static SaveFileFragmentArgs fromBundle(@NonNull Bundle bundle) {
        SaveFileFragmentArgs __result = new SaveFileFragmentArgs();
        bundle.setClassLoader(SaveFileFragmentArgs.class.getClassLoader());
        if (bundle.containsKey("image")) {
            Bitmap image;
            if (Parcelable.class.isAssignableFrom(Bitmap.class) || Serializable.class.isAssignableFrom(Bitmap.class)) {
                image = (Bitmap) bundle.get("image");
            } else {
                throw new UnsupportedOperationException(Bitmap.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
            }
            __result.arguments.put("image", image);
        } else {
            throw new IllegalArgumentException("Required argument \"image\" is missing and does not have an android:defaultValue");
        }
        if (bundle.containsKey("video")) {
            String video;
            video = bundle.getString("video");
            __result.arguments.put("video", video);
        } else {
            throw new IllegalArgumentException("Required argument \"video\" is missing and does not have an android:defaultValue");
        }
        return __result;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static SaveFileFragmentArgs fromSavedStateHandle(
            @NonNull SavedStateHandle savedStateHandle) {
        SaveFileFragmentArgs __result = new SaveFileFragmentArgs();
        if (savedStateHandle.contains("image")) {
            Bitmap image;
            image = savedStateHandle.get("image");
            __result.arguments.put("image", image);
        } else {
            throw new IllegalArgumentException("Required argument \"image\" is missing and does not have an android:defaultValue");
        }
        if (savedStateHandle.contains("video")) {
            String video;
            video = savedStateHandle.get("video");
            __result.arguments.put("video", video);
        } else {
            throw new IllegalArgumentException("Required argument \"video\" is missing and does not have an android:defaultValue");
        }
        return __result;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Bitmap getImage() {
        return (Bitmap) arguments.get("image");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public String getVideo() {
        return (String) arguments.get("video");
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle toBundle() {
        Bundle __result = new Bundle();
        if (arguments.containsKey("image")) {
            Bitmap image = (Bitmap) arguments.get("image");
            if (Parcelable.class.isAssignableFrom(Bitmap.class) || image == null) {
                __result.putParcelable("image", Parcelable.class.cast(image));
            } else if (Serializable.class.isAssignableFrom(Bitmap.class)) {
                __result.putSerializable("image", Serializable.class.cast(image));
            } else {
                throw new UnsupportedOperationException(Bitmap.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
            }
        }
        if (arguments.containsKey("video")) {
            String video = (String) arguments.get("video");
            __result.putString("video", video);
        }
        return __result;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public SavedStateHandle toSavedStateHandle() {
        SavedStateHandle __result = new SavedStateHandle();
        if (arguments.containsKey("image")) {
            Bitmap image = (Bitmap) arguments.get("image");
            if (Parcelable.class.isAssignableFrom(Bitmap.class) || image == null) {
                __result.set("image", Parcelable.class.cast(image));
            } else if (Serializable.class.isAssignableFrom(Bitmap.class)) {
                __result.set("image", Serializable.class.cast(image));
            } else {
                throw new UnsupportedOperationException(Bitmap.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
            }
        }
        if (arguments.containsKey("video")) {
            String video = (String) arguments.get("video");
            __result.set("video", video);
        }
        return __result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        SaveFileFragmentArgs that = (SaveFileFragmentArgs) object;
        if (arguments.containsKey("image") != that.arguments.containsKey("image")) {
            return false;
        }
        if (getImage() != null ? !getImage().equals(that.getImage()) : that.getImage() != null) {
            return false;
        }
        if (arguments.containsKey("video") != that.arguments.containsKey("video")) {
            return false;
        }
        if (getVideo() != null ? !getVideo().equals(that.getVideo()) : that.getVideo() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
        result = 31 * result + (getVideo() != null ? getVideo().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SaveFileFragmentArgs{"
                + "image=" + getImage()
                + ", video=" + getVideo()
                + "}";
    }

    public static final class Builder {
        private final HashMap arguments = new HashMap();

        @SuppressWarnings("unchecked")
        public Builder(@NonNull SaveFileFragmentArgs original) {
            this.arguments.putAll(original.arguments);
        }

        @SuppressWarnings("unchecked")
        public Builder(@Nullable Bitmap image, @Nullable String video) {
            this.arguments.put("image", image);
            this.arguments.put("video", video);
        }

        @NonNull
        public SaveFileFragmentArgs build() {
            SaveFileFragmentArgs result = new SaveFileFragmentArgs(arguments);
            return result;
        }

        @NonNull
        @SuppressWarnings("unchecked")
        public Builder setImage(@Nullable Bitmap image) {
            this.arguments.put("image", image);
            return this;
        }

        @NonNull
        @SuppressWarnings("unchecked")
        public Builder setVideo(@Nullable String video) {
            this.arguments.put("video", video);
            return this;
        }

        @SuppressWarnings({"unchecked","GetterOnBuilder"})
        @Nullable
        public Bitmap getImage() {
            return (Bitmap) arguments.get("image");
        }

        @SuppressWarnings({"unchecked","GetterOnBuilder"})
        @Nullable
        public String getVideo() {
            return (String) arguments.get("video");
        }
    }
}

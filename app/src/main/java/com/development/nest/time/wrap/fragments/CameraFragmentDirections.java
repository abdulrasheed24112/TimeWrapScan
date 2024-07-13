package com.development.nest.time.wrap.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;

import com.development.nest.time.wrap.R;

import java.io.Serializable;
import java.util.HashMap;

public class CameraFragmentDirections {
    private CameraFragmentDirections() {
    }

    @NonNull
    public static ActionCameraToImageViewer actionCameraToImageViewer(@Nullable Bitmap image,
                                                                      @Nullable String video) {
        return new ActionCameraToImageViewer(image, video);
    }

    public static class ActionCameraToImageViewer implements NavDirections {
        private final HashMap arguments = new HashMap();

        @SuppressWarnings("unchecked")
        private ActionCameraToImageViewer(@Nullable Bitmap image, @Nullable String video) {
            this.arguments.put("image", image);
            this.arguments.put("video", video);
        }

        @NonNull
        @SuppressWarnings("unchecked")
        public ActionCameraToImageViewer setImage(@Nullable Bitmap image) {
            this.arguments.put("image", image);
            return this;
        }

        @NonNull
        @SuppressWarnings("unchecked")
        public ActionCameraToImageViewer setVideo(@Nullable String video) {
            this.arguments.put("video", video);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        @NonNull
        public Bundle getArguments() {
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

        @Override
        public int getActionId() {
            return R.id.action_camera_to_image_viewer;
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

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            ActionCameraToImageViewer that = (ActionCameraToImageViewer) object;
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
            if (getActionId() != that.getActionId()) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
            result = 31 * result + (getVideo() != null ? getVideo().hashCode() : 0);
            result = 31 * result + getActionId();
            return result;
        }

        @Override
        public String toString() {
            return "ActionCameraToImageViewer(actionId=" + getActionId() + "){"
                    + "image=" + getImage()
                    + ", video=" + getVideo()
                    + "}";
        }
    }


}

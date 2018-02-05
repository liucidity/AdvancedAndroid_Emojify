/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

class Emojifier {

    private static final String LOG_TAG = Emojifier.class.getSimpleName();
    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;

    /**
     * Method for detecting faces in a bitmap.
     *
     * @param context The application context.
     * @param picture The picture in which to detect the faces.
     */
    static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap picture) {

        // TODO (3): Change the name of the detectFacesAndOverlayEmoji() method to detectFacesAndOverlayEmoji() and the return type from void to Bitmap

        // Create the face detector, disable tracking and enable classifications

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                //.setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        // Build the frame
        Frame frame = new Frame.Builder().setBitmap(picture).build();

        // Detect the faces
        SparseArray<Face> faces =detector.detect(frame);
        if (detector.isOperational()) {
            // Log the number of faces
            Log.d(LOG_TAG, "detectFacesAndOverlayEmoji: detector operational");
            Log.d(LOG_TAG, "detectFacesAndOverlayEmoji: number of faces = " + faces.size());


        }else
            Log.d(LOG_TAG, "detectFacesAndOverlayEmoji: facedetector not operational");



        // TODO (7): Create a variable called resultBitmap and initialize it to the original picture bitmap passed into the detectFacesAndOverlayEmoji() method
        // If there are no faces detected, show a Toast message

        Bitmap resultBitmap = picture;
        if(faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        } else {

            // Iterate through the faces
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                // Get the appropriate emoji for each face

                Bitmap emojiBitmap;
                switch (whichEmoji(face)){
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, "no emoji for face", Toast.LENGTH_SHORT).show();
                }
                // TODO (4): Create a variable called emojiBitmap to hold the appropriate Emoji bitmap and remove the call to whichEmoji()
                // TODO (5): Create a switch statement on the result of the whichEmoji() call, and assign the proper emoji bitmap to the variable you created
                // TODO (8): Call addBitmapToFace(), passing in the resultBitmap, the emojiBitmap and the Face  object, and assigning the result to resultBitmap
                resultBitmap = addBitmapToFace(resultBitmap,emojiBitmap, face);
            }
        }

        // Release the detector
        detector.release();
        // TODO (9): Return the resultBitmap
        return resultBitmap;
    }


    /**
     * Determines the closest emoji to the expression on the face, based on the
     * odds that the person is smiling and has each eye open.
     *
     * @param face The face for which you pick an emoji.
     */

    private static Emoji whichEmoji(Face face) {

        // TODO (1): Change the return type of the whichEmoji() method from void to Emoji.
        // Log all the probabilities
        Log.d(LOG_TAG, "whichEmoji: smilingProb = " + face.getIsSmilingProbability());
        Log.d(LOG_TAG, "whichEmoji: leftEyeOpenProb = "
                + face.getIsLeftEyeOpenProbability());
        Log.d(LOG_TAG, "whichEmoji: rightEyeOpenProb = "
                + face.getIsRightEyeOpenProbability());


        boolean smiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;

        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;


        // Determine and log the appropriate emoji
        Emoji emoji;
        if(smiling) {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK;
            }  else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            } else {
                emoji = Emoji.SMILE;
            }
        } else {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWN;
            }  else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else {
                emoji = Emoji.FROWN;
            }
        }


        // Log the chosen Emoji
        Log.d(LOG_TAG, "whichEmoji: " + emoji.name());

        // TODO (2): Have the method return the selected Emoji type.
        return emoji;
    }

    // TODO (6) Create a method called addBitmapToFace() which takes the background bitmap, the Emoji bitmap, and a Face object as arguments and returns the combined bitmap with the Emoji over the face.
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face){

        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(),backgroundBitmap.getConfig());

        float scaleFactor = EMOJI_SCALE_FACTOR;

        int newEmojiWidth = (int) (face.getWidth()*scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() * newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);

        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap,newEmojiWidth,newEmojiHeight,false);

        float emojiPositionX = (face.getPosition().x + face.getWidth()/2) - emojiBitmap.getWidth()/2;
        float emojiPositionY = (face.getPosition().y + face.getHeight() /2) - emojiBitmap.getHeight() /3;

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);
        return resultBitmap;
    }
    // Enum for all possible Emojis
    private enum Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }

}

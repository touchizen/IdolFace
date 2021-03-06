/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.touchizen.idolface.tflite;
import android.app.Activity;

import com.touchizen.idolface.MApplication;

import java.io.IOException;
import java.util.Locale;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

/** This TensorFlow Lite classifier works with the quantized MobileNet model. */
public class ClassifierQuantizedMobileNet extends Classifier {

  /**
   * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
   * bypass the normalization.
   */
  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 1.0f;

  /** Quantized MobileNet requires additional dequantization to the output probability. */
  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 255.0f;

  private Gender gender;
  /**
   * Initializes a {@code ClassifierQuantizedMobileNet}.
   *
   * @param activity
   */
  public ClassifierQuantizedMobileNet(
          Activity activity,
          Device device,
          Gender gender,
          int numThreads
  ) throws IOException {
    super(activity, device, gender, numThreads);
  }

  @Override
  protected String getModelPath(Gender gender) {
    // you can download this file from
    // see build.gradle for where to obtain this file. It should be auto
    // downloaded into assets.

    if (gender == Gender.FEMALE) {
        return "female_idol.tflite";
    }
    else if (gender == Gender.MALE) {
      return "male_idol.tflite";
    }

    return "total_idol.tflite";
  }

  @Override
  protected String getLabelPath(Gender gender) {

    Locale systemLocale = MApplication.instance.getResources().getConfiguration().locale;

    String strLangCode = systemLocale.getLanguage();

    if (strLangCode.toUpperCase().equals("KO")) {
      if (gender == Gender.FEMALE) {
        return "female_idol.txt";
      } else if (gender == Gender.MALE) {
        return "male_idol.txt";
      }

      return "total_idol.txt";
    }
    else {
      if (gender == Gender.FEMALE) {
        return "female_idol.en.txt";
      } else if (gender == Gender.MALE) {
        return "male_idol.en.txt";
      }

      return "total_idol.en.txt";
    }
  }

  @Override
  protected TensorOperator getPreprocessNormalizeOp() {
    return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
  }

  @Override
  protected TensorOperator getPostprocessNormalizeOp() {
    return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
  }
}

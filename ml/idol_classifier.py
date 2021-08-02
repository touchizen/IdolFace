
import numpy as np
import matplotlib.pyplot as plt

import tensorflow as tf
assert tf.__version__.startswith('2')

from tflite_model_maker import model_spec
from tflite_model_maker import image_classifier
from tflite_model_maker.image_classifier import DataLoader


def showSample(data):

    plt.figure(figsize=(10,10))
    for i, (image, label) in enumerate(data.gen_dataset().unbatch().take(25)):
        plt.subplot(5,5,i+1)
        plt.xticks([])
        plt.yticks([])
        plt.grid(False)
        plt.imshow(image.numpy(), cmap=plt.cm.gray)
        plt.xlabel(data.index_to_label[label.numpy()])
    plt.show()


# A helper function that returns 'red'/'black' depending on if its two input
# parameter matches or not.
def get_label_color(val1, val2):
    if val1 == val2:
        return 'black'
    else:
        return 'red'

# Then plot 100 test images and their predicted labels.
# If a prediction result is different from the label provided label in "test"
# dataset, we will highlight it in red color.

def showPredicted(data, model):

    plt.figure(figsize=(20, 20))
    predicts = model.predict_top_k(data)
    for i, (image, label) in enumerate(data.gen_dataset().unbatch().take(100)):
        ax = plt.subplot(10, 10, i+1)
        plt.xticks([])
        plt.yticks([])
        plt.grid(False)
        plt.imshow(image.numpy(), cmap=plt.cm.gray)

        predict_label = predicts[i][0][0]
        color = get_label_color(
            predict_label,
            data.index_to_label[label.numpy()]
            )
        ax.xaxis.label.set_color(color)
        plt.xlabel('Predicted: %s' % predict_label)
    plt.show()


def train_and_evaluate(image_path):
    #
    # Load input data specific to an on-device ML app. Split it to training data and testing data.
    #
    data = DataLoader.from_folder(image_path)
    train_data, test_data = data.split(0.9)

    #showSample(data)

    print("======================================")
    print("The number of Data =", len(data))
    print("The number of Train data =", len(train_data))
    print("The number of Test Data =", len(test_data))
    print("======================================")

    #
    # Customize the TensorFlow model.
    # Showed the architecture of model automatically.
    #
    model = image_classifier.create(train_data,epochs=6)

    #
    # Evaluate the model.
    #
    loss, accuracy = model.evaluate(test_data)
    print("======================================")
    print("Loss =", loss)
    print("Accuracy =", accuracy)
    print("======================================")

    #showPredicted(test_data, model)

    #
    # Export to TensorFlow Lite model. You could download it in the left sidebar same as the uploading part for your own use.
    #
    model.export(export_dir=image_path, with_metadata=False)


IMAGE_PATHS = [
#    "g:\\deeplearning\\selenium\\images\\female.out",
#    "g:\\deeplearning\\selenium\\images\\male.out",
    "g:\\deeplearning\\selenium\\images\\total"
    ]


def run_main():

    for onePath in IMAGE_PATHS:
        train_and_evaluate(onePath)


if __name__ == '__main__':

    # parser = argparse.ArgumentParser()
    # parser.add_argument('--eng-file', help='SRT File Path for English', default=".")
    # parser.add_argument('--kor-file', help='SRT File Path for Korean', default=".")
    # parser.add_argument('--out-file', help='SRT File Path for Output', default="output.srt")
    # args = parser.parse_args()

    try:
        run_main()
    except Exception as e:
        print("Error: {}".format(e))                        
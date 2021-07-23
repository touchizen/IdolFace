
import filetype
import os
import sys
import PIL
import PIL.Image as Image
import tensorflow as tf
import numpy as np

formats = [".jpg",".png",".jpeg"]

IMAGE_PATHS = [
    "g:\\deeplearning\\selenium\\images\\female",
    "g:\\deeplearning\\selenium\\images\\male",
    "g:\\deeplearning\\selenium\\images\\total"
    ]

def is_image(filename, verbose=False):

    data = open(filename,'rb').read(10)

    # check if file is JPG or JPEG
    if data[:3] == b'\xff\xd8\xff':
        if verbose == True:
             print(filename+" is: JPG/JPEG.")
        return True

    # check if file is PNG
    if data[:8] == b'\x89\x50\x4e\x47\x0d\x0a\x1a\x0a':
        if verbose == True:
             print(filename+" is: PNG.")
        return True

    # check if file is GIF
    if data[:6] in [b'\x47\x49\x46\x38\x37\x61', b'\x47\x49\x46\x38\x39\x61']:
        if verbose == True:
             print(filename+" is: GIF.")
        return True

    return False


def load_image(filename):
  with open(filename,mode="rb") as f:
    return np.array(f.read())


count = 1
convCount = 0
removeCount = 0
for onePath in IMAGE_PATHS:
    for (path,dirs,files) in os.walk(onePath):
        for file in files:

            fileName = os.path.splitext(file)[0]
            fileExt = os.path.splitext(file)[1]
            count = count + 1

            imagePath = path + "\\" + file

            try:
                image_contents = tf.io.read_file(imagePath)
                tf.image.decode_jpeg(image_contents)
            except Exception as e:
                print("InValid :{}".format(e),imagePath)
                if fileExt.lower() in ['.jpeg','.jpg','.png']:
                    #imagePath = path + "\\" + file
                    convPath = path + "\\" + fileName + ".jpg"
                    try:
                        im = Image.open(imagePath)
                        bg = im.convert('RGB')
                        im.close()

                        bg.save(convPath)
                        if fileExt.lower() in ['.jpeg', '.png']:
                            os.remove(imagePath) 
                            removeCount = removeCount + 1

                        convCount = convCount + 1
                    except Exception as e:
                        print("Converting Error :{} =".format(e),imagePath)

            if count % 1000 == 0:
                print("Count = ", count)

print("Total Image count=", count)
print("Total Conv count=", convCount)
print("Total Remove count=", removeCount)
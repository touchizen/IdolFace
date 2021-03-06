# 아이돌 페이스 IdolFace

<a href='https://play.google.com/store/apps/details?id=com.touchizen.idolface'><img src='https://touchizen.com/assets/images/google-play.png' alt='Get it on Google Play' height=45/></a>
<!--a href='https://f-droid.org/packages/com.touchizen.idolface'><img src='https://touchizen.com/assets/images/f-droid.png' alt='Get it on F-Droid' height=45 ></a-->

### 개요

이앱은 머신러닝을 활용해서, 아이돌 얼굴 닮은꼴을 찾아주는 앱입니다.
https://youtu.be/krlLu294n00
[![Video Label](https://touchizen.com/assets/images/idolface_yt.jpg)](https://youtu.be/krlLu294n00?t=0s)

인터넷에서 상당한 순위에 오른, 고른 약 37명의 남여 아이돌을 골라, 사진을 수집한 후에
머신러닝으로 훈련시켜 '닮은꼴'을 찾아주며, 얼굴을 변환(교환) 시켜주는 앱입니다.
   
사용자분들이 사진을 모아 올려주면, 1,000장이 넘어가는 아이돌이나, 개인을 다시 훈련시켜
닮은 꼴을 찾는 아이돌 얼굴을 증가시키는 방향으로 하겠습니다.
   
소스도 오픈할 예정이라, 코딩등 다양한 방법으로 기여할 의사가 있으신 분은 얼마든지 환영합니다.

### 주요기능

* 얼굴인식: 영상이나 사진에 얼굴이 있는지 확인하는 기능입니다.
* 아이돌 분류: v0.9에선, 37명의 아이돌과 약 40,000 장의 이미지를 학습하여, 아이돌을 분류하는 기능입니다.
* 사진찍기: 영상뿐만 아니라, 닮은꼴을 확인하기 위한 사진을 찍을 수 있습니다.
* 사진 공유: 사진을 찍은후, 소셜 미디어를 통해 공유할 수 있습니다.
* 폰 앨범: 폰에 있는 사진도 닮은꼴을 확인할 수 있습니다.
* 아이돌 추가: 추가하고 싶은 아이돌이 있다면, 필요한 기능입니다. 그러나 저희쪽에서 승인이 필요합니다.
* 이미지 업로드: 아직 경험이 없어서인지는 몰라도, 아이돌당 최소 500장 정도가 필요합니다. 
  전송된 이미지를 사용해서, 좀 더 강화된 아이돌페이스 앱을 만들어 보겠습니다.
* 로긴/로그아웃: 사진을 전송하고 공유하는 것은 저작권 관련 문제가 있으며 개인이 책임져야 합니다. 
  확인하시고 보내주시면 감사하겠습니다.

### 머시러닝 사용기술

* 얼굴인식(Face Detection): 얼굴인식기술로 구글에서 만들어진 API를 사용합니다. <br/>
    link: https://developers.google.com/ml-kit/vision/face-detection/android?hl=ko
    
* 아이돌 분류(Idol Classification): 이미지 분류 기술을 사용합니다. <br/>
    link: https://github.com/tuxxon/FlowerClassifier

### 후원 및 기여

* 코드: 코드의 추가 변경, 완전한 구조변경도 환경합니다.
* 디자인: 더 좋은 디자인을 제공해 주신다면 이역시 환영할 만한 일입니다.
* 아이돌 이미지: 학습강화를 위해 이미지 전송은 큰 기여중에 하나입니다.
* 아이디어: 괜찮은 사용자 경험을 위해 추가적인 아이디어를 보내주신다면 확인하여 반영하겠습니다.
* 기부: 서버비용 유지를 위해 기부(후원)가 필요합니다.

### 기타.

*  제작 과정을 유투브 영상으로 제작하여 공유할 예정입니다. 기다려 주시기 바랍니다.
<
* 텐서플로우로 만들어진 머신러닝 모델을 안드로이드 앱으로 제작하기 위해서, Tenforflow-lite 모델로 변경.
* 안드로이드 앱으로 포팅.
* 제작 과정을 유투브 동영상 제작 = https://youtu.be/krlLu294n00

[![Video Label](https://touchizen.com/assets/images/idolface_yt.jpg)](https://youtu.be/krlLu294n00?t=0s)

### 라이센스와 저자

* 라이센스: GPLv3
* 저자: Gordon Ahn at Touchizen.com

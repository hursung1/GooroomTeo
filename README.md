# GooroomTeo
모바일앱프로그래밍실습_구름터

SKKU, Mobile App Programming Practice
GooroomTeo, ¿Where is the smoking zone in our school?

수정 로그[2019/06/09 오후 2시쯤]
1. 드디어 구글 Map이 잘 나옵니다.
2. MainActivity에서 Firebase Database의 location info를 잘 받아오도록 수정하였습니다.
3. FirebasePost Class 수정이 있었습니다. name을 삭제했는데, 그냥 key를 받아오시면 됩니다.
4. UserRateInfo Class를 새로 만들었습니다. Firebase Database에서 User Rate 정보를 받아오기 위함입니다.
5. User Rate의 key는 작성 시간으로 하였습니다. Millisecond단위라, 거진 비슷한 시간에 작성하더라도 구분이 될 것이기 때문입니다.

문제점[2019/06/09 오후 2시쯤]
1. 아직 Firebase에서 받아온 정보를 Pin 하지 못합니다.
2. 현위치가 아직 (0, 0)으로 나옵니다.
3. User Rate를 못받아옵니다.
4. AddActivity 추가가 필요합니다.

수정 로그[2019/06/09 오후 5시쯤]
1. Pin을 누르면 RateActivity에서 해당 위치 이름과 평균 평점, 사용자들의 rate와 comment가 잘 나옵니다.
2. 의미 없는 서울 pin을 삭제하였습니다.
3. RateActivity가 이제 Landscape Mode를 지원합니다.

문제점[2019/06/09 오후 5시쯤]
1. 현위치 (0, 0)으로 나오는 문제를 수정할 필요가 있습니다.
2. AddActivity 추가가 아직 안 되었습니다.
3. MainActivity와 AddActivity Landscape mode 지원해야 합니다.

수정 로그[2019/06/12 오전 9시쯤]
1. RateActivity에서 Image 잘 불러오는 것을 확인하였습니다.

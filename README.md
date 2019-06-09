# GooroomTeo
모바일앱프로그래밍실습_구름터

SKKU, Mobile App Programming Practice
GooroomTeo, ¿Where is the smoking zone?

수정 로그[2019/06/09 오후 2시쯤]
1. 드디어 구글 Map이 잘 나옵니다.
2. MainActivity에서 Firebase Database의 location info를 잘 받아오도록 수정하였습니다.
3. FirebasePost Class 수정이 있었습니다. name을 삭제했는데, 그냥 key를 받아오시면 됩니다.
4. UserRateInfo Class를 새로 만들었습니다. Firebase Database에서 User Rate 정보를 받아오기 위함입니다.
5. User Rate의 key는 작성 시간으로 하였습니다. Millisecond단위라, 거진 비슷한 시간에 작성하더라도 구분이 될 것이기 때문입니다.

문제점
1. 아직 Firebase에서 받아온 정보를 Pin 하지 못합니다.
2. 현위치가 아직 (0, 0)으로 나옵니다.
3. User Rate를 못받아옵니다.
4. AddActivity는 git에 추가되지도 않았습니다. ㅜㅜ

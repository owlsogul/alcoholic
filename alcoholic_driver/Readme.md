dotmatrix : ./dottest spin으로 실행 => DOTM_SPIN  ioctl 매크로 호출

motor : ./motortest 1 1 (속도 : 10정도가 적당) (second)
        => second 랜덤으로 넣어주면 그 시간동안 회전하고 정지

dipswitch : read 해주면 스위치를 왼쪽부터 이진수로 따져서 on off off off off off off off이면 1, on on off off off off off off이면 3 형태로 리턴

pushbutton : 마찬가지로 read 해주면 왼쪽 위부터 오른쪽 아래로 0 ~ 8 리턴

========================================

dotmatrix : DOTM_POP, DOTM_BOMB 추가

lcd : 첫째줄은 LCD_SET_CURSOR_POS로 pos=0으로 이동 후 write, 둘째줄은 pos=16
        
led : 이상하게 커널에서 이게 적용이 안돼서 write하기 전에 계산 먼저 해줘야 함. => bytedata : 입력값 0~8

      if(bytedata != 0) {
          for(i=1; i<bytedata; i++) {
              pow = 1;
              for(j=0;j<i;j++) {
                  pow *= 2;
              }
              num += pow;
          }
          bytedata = num;
      }
        
        

package plachess.engine;

import java.util.Random;

public class BitBoardLayerTest {
    private static Random random = new Random();

    public static BitBoardLayer getRandomBitBoardLayer() {
        return new BitBoardLayer(random.nextLong());
    }

    public static void testRotate90() {
        int count = 10000;
        for(int ci = 0; ci < count; ++ci) {
            BitBoardLayer bbl = getRandomBitBoardLayer();
            BitBoardLayer rbbl = bbl.rotate90();
            for(int y = 0; y < BitBoardLayer.BS; ++y) {
                for(int x = 0; x < BitBoardLayer.BS; ++x) {
                    if(bbl.isCell(x, y) != rbbl.isCell(y, BitBoardLayer.BS-1 - x)) {
                        System.out.printf("WA:\n%s\n%s", bbl, rbbl);
                        return;
                    }
                }
            }
        }
    }

    public static void testRotate90s() {
        int count = 10000;
        for(int ci = 0; ci < count; ++ci) {
            BitBoardLayer bbl = getRandomBitBoardLayer();
            BitBoardLayer rbbl90_1 = bbl.rotate90();
            BitBoardLayer rbbl90_2 = rbbl90_1.rotate90();
            BitBoardLayer rbbl90_3 = rbbl90_2.rotate90();
            BitBoardLayer rbbl90_4 = rbbl90_3.rotate90();
            BitBoardLayer rbbl180_1 = bbl.rotate180();
            BitBoardLayer rbbl180_2 = rbbl180_1.rotate180();
            BitBoardLayer rbbl180_90 = rbbl180_1.rotate90();
            if(
                    rbbl90_2.b != rbbl180_1.b ||
                    rbbl90_4.b != bbl.b ||
                    rbbl180_2.b != bbl.b ||
                    rbbl90_3.b != rbbl180_90.b) {
                System.out.printf("WA:\n%s", bbl);
                return;
            }
        }
    }


    public static void testRotate45() {
        int count = 10000000;
        BitBoardLayer[] bs = new BitBoardLayer[count];
        for(int i = 0; i < count; ++i)
            bs[i] = getRandomBitBoardLayer();

//        System.out.println(bs[0].toString());
//        for(int i = 0; i < count; ++i) {
//            for(int y = 0; y < 8; ++y) {
//                for (int x = 0; x < 8; ++x) {
//                    int res1 = BitBoardLayer.getDiagonalFromRotated45(bs[i], x, y);
//                    int res2 = BitBoardLayer.getDiagonalFromRotated45_2(bs[i], x, y);
//                    System.out.printf("i=%d y=%d x=%d\n%d %s\n%d %s\n",
//                            i, y, x, res1, Integer.toBinaryString(res1), res2, Integer.toBinaryString(res2));
//                }
//            }
//        }

        long start, finish, timeElapsed;

        start = System.currentTimeMillis();
        int sum1 = 0;
        for(int i = 0; i < count; ++i) {
            for(int y = 0; y < 8; ++y) {
                for (int x = 0; x < 8; ++x) {
                    int res = BitBoardLayer.getDiagonalFromRotated45(bs[i], x, y);
                    sum1 += res;
                }
            }
        }
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Time " + timeElapsed + "ms");

        start = System.currentTimeMillis();
        int sum2 = 0;
        for(int i = 0; i < count; ++i) {
            for(int y = 0; y < 8; ++y) {
                for (int x = 0; x < 8; ++x) {
                    int res = BitBoardLayer.getDiagonalFromRotated45_2(bs[i], x, y);
                    sum2 += res;
                }
            }
        }
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Time " + timeElapsed + "ms");

        System.out.println(sum1 + " " + sum2);
    }

    public static void test() {
        testRotate90();
        testRotate90s();
        testRotate45();
    }
}

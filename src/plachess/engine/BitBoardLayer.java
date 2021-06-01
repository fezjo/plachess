package plachess.engine;

import java.util.Random;

public class BitBoardLayer {
    public static final int BS = 8;
    public static final int BM = 0xFF;

    public final long b;

    public BitBoardLayer(long value) {
        this.b = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != getClass()) return false;
        BitBoardLayer that = (BitBoardLayer) obj;
        return this.b == that.b;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int y = BS-1; y >= 0; --y) {
            String bs = Integer.toBinaryString(Byte.toUnsignedInt(getRow(y)));
            sb.append(rowToString(getRow(y))).append("\n");
        }
        return sb.toString();
    }

    public static String rowToString(byte row) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toBinaryString(Byte.toUnsignedInt(row)));
        sb.reverse();
        sb.append("0".repeat(BS - sb.length()));
        return sb.toString();
    }

    public byte getRow(int y) {
        return (byte)((b >> (y * BS)) & BM);
    }

    public byte getCell(int x, int y) {
        return (byte)((getRow(y) & (1 << x)) == 0 ? 0 : 1);
    }

    /*
    9 A B C D E F 0     9 1 1 1 1 1 1 1
    A B C D E F 0 1     A A 2 2 2 2 2 2
    B C D E F 0 1 2     B B B 3 3 3 3 3
    C D E F 0 1 2 3     C C C C 4 4 4 4
    D E F 0 1 2 3 4     D D D D D 5 5 5
    E F 0 1 2 3 4 5     E E E E E E 6 6
    F 0 1 2 3 4 5 6     F F F F F F F 7
    0 1 2 3 4 5 6 7     0 0 0 0 0 0 0 0

    0 0 255 0xff 0
    1 1 18302628885633695744 0xfe00000000000000 57
    2 2 70931694131085312 0xfc000000000000 50
    3 3 272678883688448 0xf80000000000 43
    4 4 1030792151040 0xf000000000 36
    5 5 3758096384 0xe0000000 29
    6 6 12582912 0xc00000 22
    7 7 32768 0x8000 15
    8 8 0 0x0 64
    9 9 72057594037927936 0x100000000000000 56
    10 A 844424930131968 0x3000000000000 48
    11 B 7696581394432 0x70000000000 40
    12 C 64424509440 0xf00000000 32
    13 D 520093696 0x1f000000 24
    14 E 4128768 0x3f0000 16
    15 F 32512 0x7f00 8
     */
    private static final long[] ROTATE45_MASK = {
            0xFFL,0xFe00000000000000L,0xFc000000000000L,0xF80000000000L,0xF000000000L,0xe0000000L,0xc00000L,0x8000L,
            0x0L, 0x100000000000000L,0x3000000000000L,0x70000000000L,0xF00000000L,0x1F000000L,0x3F0000L,0x7F00L
    };
    private static final long[] ROTATE45_SHIFT = {
            0, 57, 50, 43, 36, 29, 22, 15, 64, 56, 48, 40, 32, 24, 16, 8
    };

    public static byte getDiagonalFromRotated45(BitBoardLayer board, int x, int y) {
        int i = ((x - y) + 16) & 15;
        return (byte)((board.b & ROTATE45_MASK[i]) >>> ROTATE45_SHIFT[i]);
    }
    public static byte getDiagonalFromRotated45_2(BitBoardLayer board, int x, int y) {
        int i = ((x - y) + 16) & 15;
        int last3 = i & 7;
        boolean otherDiagonal = (i & 8) == 0;
        int dLength = otherDiagonal ? 8 - last3 : last3;
        int shift = otherDiagonal ? 8 - dLength : 0;
        int row_i = (8 - last3) & 7;
        long row = board.b >> (8 * row_i);
        return (byte) ((row >> shift) & ((1 << dLength) - 1));
    }

    /*
    0 F E D C B A 9     1 1 1 1 1 1 1 9
    1 0 F E D C B A     2 2 2 2 2 2 A A
    2 1 0 F E D C B     3 3 3 3 3 B B B
    3 2 1 0 F E D C     4 4 4 4 C C C C
    4 3 2 1 0 F E D     5 5 5 D D D D D
    5 4 3 2 1 0 F E     6 6 E E E E E E
    6 5 4 3 2 1 0 F     7 F F F F F F F
    7 6 5 4 3 2 1 0     0 0 0 0 0 0 0 0

    0 0 255 0xff 0
    1 1 9151314442816847872 0x7f00000000000000 56
    2 2 17732923532771328 0x3f000000000000 48
    3 3 34084860461056 0x1f0000000000 40
    4 4 64424509440 0xf00000000 32
    5 5 117440512 0x7000000 24
    6 6 196608 0x30000 16
    7 7 256 0x100 8
    8 8 0 0x0 64
    9 9 9223372036854775808 0x8000000000000000 63
    10 A 54043195528445952 0xc0000000000000 54
    11 B 246290604621824 0xe00000000000 45
    12 C 1030792151040 0xf000000000 36
    13 D 4160749568 0xf8000000 27
    14 E 16515072 0xfc0000 18
     */
    private static final long[] ROTATE45A_MASK = {
            0xFFL,0x7F00000000000000L,0x3F000000000000L,0x1F0000000000L,0xF00000000L,0x7000000L,0x30000L,0x100L,
            0x0L,0x8000000000000000L,0xc0000000000000L,0xe00000000000L,0xF000000000L,0xF8000000L,0xFc0000L
    };
    private static final long[] ROTATE45A_SHIFT = {
            0, 56, 48, 40, 32, 24, 16, 8, 64, 63, 54, 45, 36, 27, 18
    };

    public static byte getDiagonalFromRotated45A(BitBoardLayer board, int x, int y) {
        int i = (x + y) ^ 7;
        return (byte)((board.b & ROTATE45A_MASK[i]) >>> ROTATE45A_SHIFT[i]);
    }

    /** because rotate45 is pseudo operation, it has to be always done last, it is not commutative */
    public BitBoardLayer rotate45() {
        final long k1 = 0xAAAAAAAAAAAAAAAAL;
        final long k2 = 0xCCCCCCCCCCCCCCCCL;
        final long k4 = 0xF0F0F0F0F0F0F0F0L;

        long x = b;
        x ^= k1 & (x ^ Long.rotateRight(x,  8));
        x ^= k2 & (x ^ Long.rotateRight(x, 16));
        x ^= k4 & (x ^ Long.rotateRight(x, 32));
        return new BitBoardLayer(x);
    }

    public BitBoardLayer rotate45A() {
        final long k1 = 0x5555555555555555L;
        final long k2 = 0x3333333333333333L;
        final long k4 = 0x0F0F0F0F0F0F0F0FL;

        long x = b;
        x ^= k1 & (x ^ Long.rotateRight(x,  8));
        x ^= k2 & (x ^ Long.rotateRight(x, 16));
        x ^= k4 & (x ^ Long.rotateRight(x, 32));
        return new BitBoardLayer(x);
    }

    public BitBoardLayer rotate90() {
        return flipVertical().flipDiagonal();
    }

    public BitBoardLayer rotate90A() {
        return flipVertical().flipDiagonalA();
    }

    public BitBoardLayer rotate180() {
        return new BitBoardLayer(Long.reverse(this.b));
    }

    public BitBoardLayer rotate(int angle) {
        if(angle < 6) {
            BitBoardLayer result = this;
            if ((angle & 4) != 0) result = this.rotate180();
            if ((angle & 2) != 0) result = this.rotate90();
            if ((angle & 1) != 0) result = this.rotate45();
            return result;
        } else if(angle == 6) return this.rotate90A();
        else if(angle == 7) return this.rotate45A();
        else return null;
    }


    public BitBoardLayer flipHorizontal() {
        return new BitBoardLayer(Long.reverseBytes(this.b));
    }

    public BitBoardLayer flipVertical() {
        return flipHorizontal().rotate180();
    }

    public BitBoardLayer flipDiagonal() {
        final long k1 = 0x5500550055005500L;
        final long k2 = 0x3333000033330000L;
        final long k4 = 0x0F0F0F0F00000000L;

        long t, x = this.b;
        t  = k4 & (x ^ (x << 28));
        x ^=       t ^ (t >> 28) ;
        t  = k2 & (x ^ (x << 14));
        x ^=       t ^ (t >> 14) ;
        t  = k1 & (x ^ (x <<  7));
        x ^=       t ^ (t >>  7) ;
        return new BitBoardLayer(x);
    }

    public BitBoardLayer flipDiagonalA() {
        final long k1 = 0xaa00aa00aa00aa00L;
        final long k2 = 0xcccc0000cccc0000L;
        final long k4 = 0xF0F0F0F00F0F0F0FL;

        long t, x = this.b;
        t  =       x ^ (x << 36) ;
        x ^= k4 & (t ^ (x >> 36));
        t  = k2 & (x ^ (x << 18));
        x ^=       t ^ (t >> 18) ;
        t  = k1 & (x ^ (x <<  9));
        x ^=       t ^ (t >>  9) ;
        return new BitBoardLayer(x);
    }
}

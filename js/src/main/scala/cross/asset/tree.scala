 //1
package cross.asset //2
 //3
import cross.mvc.TreeVariations._
import cross.mvc.{Asset, TreeAsset}
import cross.vec._ //6
 //7
object tree { //8
val branches = List( //9
    TreeAsset(asset = Asset("/image/0L+0-1.png"), level = 0, rotation = +0, variation = Left____, rootAnchor = 15 xy 29, branchAnchor = 10 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/0R+0-1.png"), level = 0, rotation = +0, variation = Right___, rootAnchor = 16 xy 29, branchAnchor = 21 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/0S+0-1.png"), level = 0, rotation = +0, variation = Straight, rootAnchor = 16 xy 29, branchAnchor = 16 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1L+0-1.png"), level = 1, rotation = +0, variation = Left____, rootAnchor = 14 xy 29, branchAnchor = 10 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1L+1-1.png"), level = 1, rotation = +1, variation = Left____, rootAnchor = 13 xy 28, branchAnchor = 21 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1L-1-1.png"), level = 1, rotation = -1, variation = Left____, rootAnchor = 21 xy 27, branchAnchor = 5 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1R+0-1.png"), level = 1, rotation = +0, variation = Right___, rootAnchor = 17 xy 29, branchAnchor = 21 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1R+1-1.png"), level = 1, rotation = +1, variation = Right___, rootAnchor = 11 xy 27, branchAnchor = 26 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1R-1-1.png"), level = 1, rotation = -1, variation = Right___, rootAnchor = 18 xy 28, branchAnchor = 10 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1S+0-1.png"), level = 1, rotation = +0, variation = Straight, rootAnchor = 16 xy 29, branchAnchor = 16 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1S+1-1.png"), level = 1, rotation = +1, variation = Straight, rootAnchor = 9 xy 28, branchAnchor = 21 xy 3, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/1S-1-1.png"), level = 1, rotation = -1, variation = Straight, rootAnchor = 22 xy 28, branchAnchor = 10 xy 3, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2L+0-1.png"), level = 2, rotation = +0, variation = Left____, rootAnchor = 16 xy 29, branchAnchor = 9 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2L+1-1.png"), level = 2, rotation = +1, variation = Left____, rootAnchor = 12 xy 29, branchAnchor = 19 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2L+2-1.png"), level = 2, rotation = +2, variation = Left____, rootAnchor = 9 xy 28, branchAnchor = 24 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2L-1-1.png"), level = 2, rotation = -1, variation = Left____, rootAnchor = 22 xy 28, branchAnchor = 6 xy 7, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2L-2-1.png"), level = 2, rotation = -2, variation = Left____, rootAnchor = 27 xy 23, branchAnchor = 4 xy 9, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2R+0-1.png"), level = 2, rotation = +0, variation = Right___, rootAnchor = 15 xy 29, branchAnchor = 21 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2R+1-1.png"), level = 2, rotation = +1, variation = Right___, rootAnchor = 8 xy 28, branchAnchor = 25 xy 7, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2R+2-1.png"), level = 2, rotation = +2, variation = Right___, rootAnchor = 4 xy 23, branchAnchor = 27 xy 9, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2R-1-1.png"), level = 2, rotation = -1, variation = Right___, rootAnchor = 19 xy 29, branchAnchor = 12 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2R-2-1.png"), level = 2, rotation = -2, variation = Right___, rootAnchor = 22 xy 28, branchAnchor = 7 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2S+0-1.png"), level = 2, rotation = +0, variation = Straight, rootAnchor = 16 xy 29, branchAnchor = 17 xy 2, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2S+1-1.png"), level = 2, rotation = +1, variation = Straight, rootAnchor = 9 xy 27, branchAnchor = 21 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2S+2-1.png"), level = 2, rotation = +2, variation = Straight, rootAnchor = 6 xy 24, branchAnchor = 24 xy 6, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2S-1-1.png"), level = 2, rotation = -1, variation = Straight, rootAnchor = 22 xy 27, branchAnchor = 10 xy 4, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/2S-2-1.png"), level = 2, rotation = -2, variation = Straight, rootAnchor = 25 xy 24, branchAnchor = 6 xy 6, flowerAnchors = List()),
    TreeAsset(asset = Asset("/image/3L+0-1.png"), level = 3, rotation = +0, variation = Left____, rootAnchor = 17 xy 29, branchAnchor = 13 xy 2, flowerAnchors = List(11 xy 4, 13 xy 16)),
    TreeAsset(asset = Asset("/image/3L+1-1.png"), level = 3, rotation = +1, variation = Left____, rootAnchor = 12 xy 29, branchAnchor = 19 xy 3, flowerAnchors = List(10 xy 20, 14 xy 12, 19 xy 7)),
    TreeAsset(asset = Asset("/image/3L+2-1.png"), level = 3, rotation = +2, variation = Left____, rootAnchor = 8 xy 25, branchAnchor = 23 xy 4, flowerAnchors = List(9 xy 16, 17 xy 13, 21 xy 6)),
    TreeAsset(asset = Asset("/image/3L+3-1.png"), level = 3, rotation = +3, variation = Left____, rootAnchor = 5 xy 23, branchAnchor = 27 xy 7, flowerAnchors = List(14 xy 16, 21 xy 8)),
    TreeAsset(asset = Asset("/image/3L-1-1.png"), level = 3, rotation = -1, variation = Left____, rootAnchor = 23 xy 26, branchAnchor = 8 xy 4, flowerAnchors = List(11 xy 8, 14 xy 19)),
    TreeAsset(asset = Asset("/image/3L-2-1.png"), level = 3, rotation = -2, variation = Left____, rootAnchor = 25 xy 23, branchAnchor = 4 xy 8, flowerAnchors = List(6 xy 11, 17 xy 16)),
    TreeAsset(asset = Asset("/image/3L-3-1.png"), level = 3, rotation = -3, variation = Left____, rootAnchor = 29 xy 19, branchAnchor = 3 xy 12, flowerAnchors = List(8 xy 12, 15 xy 20)),
    TreeAsset(asset = Asset("/image/3R+0-1.png"), level = 3, rotation = +0, variation = Right___, rootAnchor = 14 xy 29, branchAnchor = 18 xy 2, flowerAnchors = List(16 xy 16, 19 xy 3)),
    TreeAsset(asset = Asset("/image/3R+1-1.png"), level = 3, rotation = +1, variation = Right___, rootAnchor = 8 xy 26, branchAnchor = 24 xy 4, flowerAnchors = List(21 xy 13, 22 xy 5)),
    TreeAsset(asset = Asset("/image/3R+2-1.png"), level = 3, rotation = +2, variation = Right___, rootAnchor = 6 xy 23, branchAnchor = 27 xy 8, flowerAnchors = List(18 xy 18, 26 xy 11)),
    TreeAsset(asset = Asset("/image/3R+3-1.png"), level = 3, rotation = +3, variation = Right___, rootAnchor = 2 xy 19, branchAnchor = 28 xy 12, flowerAnchors = List(17 xy 19, 23 xy 13)),
    TreeAsset(asset = Asset("/image/3R-1-1.png"), level = 3, rotation = -1, variation = Right___, rootAnchor = 19 xy 29, branchAnchor = 12 xy 3, flowerAnchors = List(14 xy 9, 15 xy 4, 18 xy 15)),
    TreeAsset(asset = Asset("/image/3R-2-1.png"), level = 3, rotation = -2, variation = Right___, rootAnchor = 23 xy 25, branchAnchor = 8 xy 4, flowerAnchors = List(9 xy 4, 15 xy 12, 20 xy 14)),
    TreeAsset(asset = Asset("/image/3R-3-1.png"), level = 3, rotation = -3, variation = Right___, rootAnchor = 26 xy 23, branchAnchor = 4 xy 7, flowerAnchors = List(5 xy 9, 13 xy 9, 19 xy 17)),
    TreeAsset(asset = Asset("/image/3S+0-1.png"), level = 3, rotation = +0, variation = Straight, rootAnchor = 16 xy 29, branchAnchor = 16 xy 2, flowerAnchors = List(14 xy 14, 17 xy 6)),
    TreeAsset(asset = Asset("/image/3S+1-1.png"), level = 3, rotation = +1, variation = Straight, rootAnchor = 9 xy 28, branchAnchor = 20 xy 4, flowerAnchors = List(17 xy 16, 19 xy 6)),
    TreeAsset(asset = Asset("/image/3S+2-1.png"), level = 3, rotation = +2, variation = Straight, rootAnchor = 7 xy 25, branchAnchor = 26 xy 6, flowerAnchors = List(17 xy 18, 20 xy 9, 25 xy 7)),
    TreeAsset(asset = Asset("/image/3S+3-1.png"), level = 3, rotation = +3, variation = Straight, rootAnchor = 3 xy 22, branchAnchor = 27 xy 11, flowerAnchors = List(16 xy 18, 22 xy 10)),
    TreeAsset(asset = Asset("/image/3S-1-1.png"), level = 3, rotation = -1, variation = Straight, rootAnchor = 22 xy 28, branchAnchor = 11 xy 4, flowerAnchors = List(12 xy 6, 14 xy 15)),
    TreeAsset(asset = Asset("/image/3S-2-1.png"), level = 3, rotation = -2, variation = Straight, rootAnchor = 24 xy 25, branchAnchor = 5 xy 6, flowerAnchors = List(7 xy 8, 13 xy 12, 16 xy 19)),
    TreeAsset(asset = Asset("/image/3S-3-1.png"), level = 3, rotation = -3, variation = Straight, rootAnchor = 28 xy 22, branchAnchor = 4 xy 11, flowerAnchors = List(5 xy 13, 11 xy 13, 16 xy 18)) //10
) //11
} //12

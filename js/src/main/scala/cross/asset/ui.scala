 //1
package cross.asset //2
 //3
import cross.mvc.Asset //4
 //5
object ui { //6
  val `asset-loading-disabled` = Asset("/image/asset-loading-disabled.png")
  val `asset-loading-normal` = Asset("/image/asset-loading-normal.png")
  val `asset-progress-empty` = Asset("/image/asset-progress-empty.png")
  val `asset-progress-full` = Asset("/image/asset-progress-full.png")
  val `asset-progress-shine` = Asset("/image/asset-progress-shine.png")
  val `asset-sky` = Asset("/image/asset-sky.png")
  val `asset-start-hover` = Asset("/image/asset-start-hover.png")
  val `asset-start-normal` = Asset("/image/asset-start-normal.png")
  val `asset-start-pressed` = Asset("/image/asset-start-pressed.png")
  val `asset-tree-hover` = Asset("/image/asset-tree-hover.png")
  val `asset-tree-normal` = Asset("/image/asset-tree-normal.png")
  val `asset-tree-pressed` = Asset("/image/asset-tree-pressed.png") //7
val all = List(`asset-loading-disabled`, `asset-loading-normal`, `asset-progress-empty`, `asset-progress-full`, `asset-progress-shine`, `asset-sky`, `asset-start-hover`, `asset-start-normal`, `asset-start-pressed`, `asset-tree-hover`, `asset-tree-normal`, `asset-tree-pressed`) //8
} //9

package adlive.domain.support

/**
 * 値オブジェクトを表すトレイト
 */
trait ValueObject {

  /**
   * ハッシュコードを返す
   *
   * @return ハッシュコード
   */
  override def hashCode: Int

  /**
   * 指定されたオブジェクトと等価であるかを判定する
   *
   * @param that オブジェクト
   * @return 等価である場合はtrue
   */
  override def equals(that: Any): Boolean

}
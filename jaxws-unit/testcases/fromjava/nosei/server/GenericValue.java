/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromjava.nosei.server;


public final class GenericValue<T> {

  /**
   * The value contained in the holder.
   **/
  public T value;
    
  /**
   * Creates a new holder with a <code>null</code> value.
   **/
  public GenericValue() {
  }

  /**
   * Create a new holder with the specified value.
   *
   * @param value The value to be stored in the holder.
   **/
  public GenericValue(T value) {
      this.value = value;
  }
}

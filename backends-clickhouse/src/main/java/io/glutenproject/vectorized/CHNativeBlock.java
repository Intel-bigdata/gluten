/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.glutenproject.vectorized;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.vectorized.ColumnVector;
import org.apache.spark.sql.vectorized.ColumnarBatch;

import java.util.Optional;

public class CHNativeBlock {
  private long blockAddress;

  public CHNativeBlock(long blockAddress) {
    this.blockAddress = blockAddress;
  }

  public static Optional<CHNativeBlock> fromColumnarBatch(ColumnarBatch batch) {
    if (batch.numCols() == 0 || !(batch.column(0) instanceof CHColumnVector)) {
      return Optional.empty();
    }
    CHColumnVector columnVector = (CHColumnVector) batch.column(0);
    return Optional.of(new CHNativeBlock(columnVector.getBlockAddress()));
  }

  private native int nativeNumRows(long blockAddress);

  public int numRows() {
    return nativeNumRows(blockAddress);
  }

  ;

  private native int nativeNumColumns(long blockAddress);

  public int numColumns() {
    return nativeNumColumns(blockAddress);
  }

  private native String nativeColumnType(long blockAddress, int position);

  public String getTypeByPosition(int position) {
    return nativeColumnType(blockAddress, position);
  }

  private native long nativeTotalBytes(long blockAddress);

  public long totalBytes() {
    return nativeTotalBytes(blockAddress);
  }

  public native void nativeClose(long blockAddress);

  public void close() {
    if (blockAddress != 0) {
      nativeClose(blockAddress);
      blockAddress = 0;
    }
  }

  public ColumnarBatch toColumnarBatch() {
    ColumnVector[] vectors = new ColumnVector[numColumns()];
    for (int i = 0; i < numColumns(); i++) {
      vectors[i] = new CHColumnVector(DataType.fromDDL(getTypeByPosition(i)), blockAddress, i);
    }
    int numRows = 0;
    if (numColumns() != 0) {
      numRows = numRows();
    }
    return new ColumnarBatch(vectors, numRows);
  }
}

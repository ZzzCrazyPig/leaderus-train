package com.crazypig.httpserver.utils;

/**
 * 
 * @author CrazyPig
 * @since 2017-03-06
 *
 */
public class ByteUtil {
	
	/**
	 * search target byte array from source byte array, if match, return start index from source byte array 
	 * @param source the byte array to be searched from
	 * @param target the byte array to be searched for
	 * @return if match ? ${start_index_from_source_byte_array} : -1
	 */
	public static int indexOf(byte[] source, byte[] target) {
		return indexOf(source, 0, source.length, target, 0, target.length, 0);
	}
	
	public static int indexOf(byte[] source, byte[] target, int fromIndex) {
		return indexOf(source, 0, source.length, target, 0, target.length, fromIndex);
	}
	
	/**
	 * Tips: copy from JDK String.indexOf</br>
	 * search target byte array from source byte array, if match, return start index from source byte array
	 * @param source
	 * @param sourceOffset
	 * @param sourceCount
	 * @param target
	 * @param targetOffset
	 * @param targetCount
	 * @param fromIndex
	 * @return
	 */
	public static int indexOf(byte[] source, int sourceOffset, int sourceCount, 
			byte[] target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}
		
		byte first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);
		
		for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
		
		return -1;
	}

}

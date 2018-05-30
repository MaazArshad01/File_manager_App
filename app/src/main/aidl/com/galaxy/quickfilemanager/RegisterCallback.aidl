// RegisterCallback.aidl
package com.galaxy.filemanagerstorageexplorer;

// Declare any non-default types here with import statements
import com.galaxy.filemanagerstorageexplorer.ProgressListener;
import com.galaxy.filemanagerstorageexplorer.Utils.DataPackage;
interface RegisterCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void registerCallBack(in ProgressListener p);
       List<DataPackage> getCurrent();
}

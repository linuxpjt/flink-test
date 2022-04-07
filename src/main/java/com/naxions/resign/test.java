package com.naxions.resign;

import java.util.Arrays;

public class test {
    public int removeDumplicates(int[] nums) {
        int n = 0;

        for (int i=1;i<nums.length;i++) {
            if (nums[n] == nums[i]){

            }
            else {
                n+=1;
                nums[n]=nums[i];
            }
        }
        System.out.println(Arrays.toString(nums));
        return n+1;
    }

    public static void main(String[] args) {
        int[] a = new int[] {1,1,2,3,3,3,4};
        test test = new test();
        int i = test.removeDumplicates(a);
        System.out.println(i);
    }
}

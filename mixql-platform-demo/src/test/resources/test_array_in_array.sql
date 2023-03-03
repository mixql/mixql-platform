let arr = [TRUE, [TRUE, "gg", 12], 12];
print("first element in array is " || $arr[0]);

print("second element in array is : " || $arr[1]);

print("third element in array is : " || $arr[2]);

print("second element of nested array that is second element of original array: "
    || $arr[1][1]);

print($arr);
<?php

/*konversi string untuk special characters yang dikecualikan.*/

$string_ = "177_testing judul 1 , 3 , & e % , . # @ & ( , )";
//$string_ = ['1', '7', '%'];
$string = str_split($string_, 1);
/*print_r( $string );
echo "<br>";*/

$allow = [' ', '&', '(', ')', '-', '_', ',', '.', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
$replace ='_';

$result = array_diff($string, $allow); 
/*print_r( $result );
echo "<br>";*/
$temp_string = str_replace($result, $replace, $string);
/*echo "<br>";
print_r( $temp_string );
echo "<br>";*/
$new_string = implode('',$temp_string);


echo "String: ".$string_;
echo "<br>";
echo "Hasil : ".$new_string;


/*print_r( str_replace($result, $replace, $string) );
echo "<br>";

echo $string;

echo "<br>";*/

//echo implode('',$string);

/*echo "<br>";
$result = array_diff($string, $allow); 
print_r( $result );*/


/*$array1 = "Orange";
$array2 = array("Apple","Grapes","Orange","Pineapple");
if(in_array($array1,$array2)){
    echo $array1.' exists in array2';
}else{
    echo $array1.'does not exists in array2';
}
*/


?>
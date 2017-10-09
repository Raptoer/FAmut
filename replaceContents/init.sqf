|1|
// F3 - Medical Systems Support
// Credits: Please see the F3 online manual (http://www.ferstaberinde.com/f3/en/)

// SWS Config Settings
// How many extra FirstAidKits (FAKS) each player should receive when using the F3 Simple Wounding System:
f_wound_extraFAK = 2;

[] execVM "f\medical\medical_init.sqf";
|1|
// ====================================================================================
// F3 - Revive
[] execVM "f\medical\init.sqf";
|2|
// ====================================================================================

// F3 - Name Tags
// Credits: Please see the F3 online manual (http://www.ferstaberinde.com/f3/en/)

[] execVM "f\nametag\f_nametags.sqf";
|2|
// ====================================================================================

// F3 - Name Tags
[] execVM "f\nametag\f_nametagInit.sqf";

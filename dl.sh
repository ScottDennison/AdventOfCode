#!/bin/bash

function doDayFetch {
	userId="$1";
	sessionToken="$2";
	year="$3";
	rawDay="$4";
	dayUnzerod=$(printf "%d" "${rawDay}");
	dayZerod=$(printf "%02d" "${rawDay}");
	directoryPath="data/year${year}/day${dayZerod}/io/users/${userId}";
	mkdir -v -p "${directoryPath}";
	curl --insecure --output "${directoryPath}/input.txt"  -H "Cookie: session=${sessionToken}" "https://adventofcode.com/${year}/day/${dayUnzerod}/input";
}

function doYearFetch {
	userId="$1";
	sessionToken="$2";
	year="$3";
	for day in `seq 1 25`; do
		doDayFetch "${userId}" "${sessionToken}" "${year}" "${day}";
	done;
}

function doUserFetch {
	userId="$1";
	sessionToken="$2";
	for year in `seq 2015 2020`; do
		doYearFetch "${userId}" "${sessionToken}" "${year}";
	done;
}

function main {
	doUserFetch "1361797" "<redacted>";
	doUserFetch "1361805" "<redacted>";
	doUserFetch "1361808" "<redacted>";
};

main;

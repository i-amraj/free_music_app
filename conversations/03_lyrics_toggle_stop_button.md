# Task 03: Lyrics Toggle & Dismiss Button Fix

## Date / Time
September 2026

## Issue Description
- When lyrics overlay was opened during song playback, there was no clear button or UI option to close/dismiss the lyrics view back to the full player controls.

## Changes Implemented
1. Modified `Player.kt` and `LyricsHelper.kt` UI components.
2. Added a floating dismiss/close button (`Icon(R.drawable.close)`) on the lyrics screen overlay.
3. Enabled tapping anywhere outside lyrics or tapping the dismiss button to toggle back to standard player controls.

## Outcome
- Users can easily open and close lyrics view anytime during song playback.

package com.diverger.mig_android_sdk.ui.reservations

// MARK: - Document Type

enum class DocumentType {
    DNI_ID, NIE, PASSPORT;

    fun validate(value: String): Boolean = when (this) {
        DNI_ID   -> DocumentValidator.validateDNI(value) || DocumentValidator.validateEuropeanID(value)
        NIE      -> DocumentValidator.validateNIE(value)
        PASSPORT -> DocumentValidator.validatePassport(value)
    }
}

// MARK: - Validator

object DocumentValidator {

    // MARK: DNI — 8 digits + check letter with digit verification

    fun validateDNI(value: String): Boolean {
        val upper = value.uppercase()
        if (!Regex("^[0-9]{8}[A-Z]$").matches(upper)) return false
        return checkDigitValid(upper)
    }

    // MARK: NIE — X/Y/Z + 7 digits + check letter with digit verification

    fun validateNIE(value: String): Boolean {
        val upper = value.uppercase()
        if (!Regex("^[XYZ][0-9]{7}[A-Z]$").matches(upper)) return false
        return checkDigitValid(upper)
    }

    private fun checkDigitValid(upper: String): Boolean {
        val validChars = "TRWAGMYFPDXBNJZSQVHLCKET"
        var normalized = upper
        when {
            normalized.startsWith("X") -> normalized = "0" + normalized.drop(1)
            normalized.startsWith("Y") -> normalized = "1" + normalized.drop(1)
            normalized.startsWith("Z") -> normalized = "2" + normalized.drop(1)
        }
        val number = normalized.dropLast(1).toIntOrNull() ?: return false
        val expected = validChars[number % 23]
        return upper.last() == expected
    }

    // MARK: European national ID cards

    fun validateEuropeanID(value: String): Boolean {
        val input = value.uppercase()
        return euIdPatterns.any { input.matches(Regex(it, RegexOption.IGNORE_CASE)) }
    }

    private val euIdPatterns = listOf(
        "^[A-Z][A-Z0-9]{8}$",                    // AT – Personalausweis
        "^[0-9]{12}$",                             // BE – eID / FR – CNI new / LU
        "^[0-9]{10}$",                             // BG – EGN / DK – CPR / SE personnummer
        "^[0-9]{13}$",                             // CH – AHV / LU / RO – CNP / SI – EMŠO
        "^[KP][0-9]{8}$",                          // CY
        "^[0-9]{9,10}$",                           // CZ / SK
        "^[A-Z0-9]{9}$",                           // DE – Personalausweis / NL
        "^[0-9]{11}$",                             // EE / HR – OIB / LT / NO
        "^[0-9]{6}[-+A][0-9]{3}[0-9A-FHJ-NPR-Y]$", // FI – HETU
        "^[0-9F][0-9]{11}$",                       // FR – CNI old
        "^[A-Z]{1,2}[0-9]{6,7}$",                 // GR
        "^[A-Z]{2}[0-9]{6}[A-Z]$",                // HR – card number
        "^[0-9]{6}[A-Z]{2}$",                      // HU
        "^[0-9]{7}[A-Z]{1,2}$",                   // IE – PPS
        "^[A-Z]{2}[0-9]{5}[A-Z]{2}$",             // IT – CIE
        "^[A-Z]{2}[0-9]{7}[A-Z]{2}$",             // IT – CIE alt
        "^[0-9]{6}-[0-9]{5}$",                    // LV – old
        "^[3-9][0-9]{10}$",                        // LV – new
        "^[0-9]{7}[A-Z]$",                         // MT
        "^[A-Z]{3}[0-9]{6}$",                      // PL
        "^[0-9]{8}[0-9][A-Z]{2}[0-9]$",           // PT – Cartão de Cidadão
        "^[0-9]{12}$",                              // SE – 12-digit personnummer
        "^[0-9]{6}[-+][0-9]{4}$",                  // SE – with separator
    )

    // MARK: Passport — per-country regex (mirrors validator.js)

    fun validatePassport(value: String): Boolean {
        val input = value.uppercase()
        return passportPatterns.any { input.matches(Regex(it, RegexOption.IGNORE_CASE)) }
    }

    private val passportPatterns = listOf(
        "^A[0-9]{8}$",                             // AF
        "^[A-Z]{2}[0-9]{7}$",                      // AM, BY, JP, LY, PK, PL
        "^[A-Z]{3}[0-9]{6}[A-Z0-9]$",              // AR
        "^[A-Z][0-9]{7}$",                          // AT, AU, CH, IN
        "^[A-Z]{2}[0-9]{6}$",                       // BE, BR, CA, NZ, UA
        "^[0-9]{9}$",                                // BG, DK, DZ, GB, RU, US
        "^[GE][0-9]{8}$",                            // CN
        "^[KPE][0-9]{8}$",                           // CY
        "^[0-9]{8}$",                                // CZ, SE, SK
        "^[CFGHJKLMNPRTVWXYZ0-9]{9}$",              // DE
        "^[A-Z][0-9]{7}$|^[A-Z]{2}[0-9]{7}$",      // EE
        "^[A-Z0-9]{9}$",                             // ES, IT, LU
        "^[A-Z0-9][0-9]{8}$",                        // FI
        "^[0-9]{2}[A-Z]{2}[0-9]{5}$",               // FR
        "^[AEK][0-9]{7}$",                           // GR
        "^[A-Z]{2}[0-9]{6}[A-Z]$",                  // HR
        "^[A-Z]{2}[0-9]{6}$|^[A-Z]{2}[0-9]{7}$",   // HU
        "^[A-Z0-9]{2}[0-9]{7}$",                    // IE
        "^[A-Z][0-9]{8}$",                           // IR, MX, TH, TR
        "^A[0-9]{7}$",                               // IS
        "^[MS][0-9]{8}$",                            // KR
        "^[A-Z0-9]{8}$",                             // LT
        "^[A-Z0-9]{2}[0-9]{7}$",                    // LV
        "^[KL][0-9]{8}$",                            // MO
        "^[AHK][0-9]{8}$",                           // MY
        "^[A-Z]{2}[0-9]{7}$|^[A-Z][0-9]{8}$",      // MZ
        "^[A-Z]{2}[A-Z0-9][0-9]{6}[A-Z]$",         // NL
        "^[A-Z]{1,2}[0-9]{6,7}[A-Z]?$",             // PH
        "^[A-Z][0-9]{6}$",                           // PT
        "^[0-9]{8,9}$",                              // RO
        "^[A-Z][0-9]{7}$",                           // SL
    )
}

package org.jetbrains.kotlin.ir.backend.jvm.serialization

import org.jetbrains.kotlin.backend.common.serialization.GlobalDeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureSerializer
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns

class JvmGlobalDeclarationTable(signatureSerializer: IdSignatureSerializer, builtIns: IrBuiltIns)
    : GlobalDeclarationTable(signatureSerializer, JvmManglerIr) {
    init {
        loadKnownBuiltins(builtIns)
    }
}
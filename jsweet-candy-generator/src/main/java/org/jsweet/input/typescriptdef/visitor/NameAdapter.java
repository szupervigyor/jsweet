package org.jsweet.input.typescriptdef.visitor;

import static org.jsweet.JSweetDefTranslatorConfig.STRING_TYPES_INTERFACE_NAME;
import static org.jsweet.input.typescriptdef.util.Util.checkAndAdjustDeclarationName;
import static org.jsweet.input.typescriptdef.util.Util.toJavaName;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jsweet.input.typescriptdef.ast.Context;
import org.jsweet.input.typescriptdef.ast.ModuleDeclaration;
import org.jsweet.input.typescriptdef.ast.Scanner;
import org.jsweet.input.typescriptdef.ast.TypeDeclaration;
import org.jsweet.input.typescriptdef.ast.TypeReference;

public class NameAdapter extends Scanner {

	public NameAdapter(Context context) {
		super(context);
	}

	public NameAdapter(Scanner parentScanner) {
		super(parentScanner);
	}

	@Override
	public void visitModuleDeclaration(ModuleDeclaration declaration) {
		checkAndAdjustDeclarationName(declaration, true);
		if (declaration.isQuotedName()) {
			context.externalModules.put(getCurrentModuleName(), declaration.getOriginalName());
		}
		super.visitModuleDeclaration(declaration);
	}

	@Override
	public void visitTypeReference(TypeReference typeReference) {
		String adaptedName = getAdaptedName(typeReference);
		if (adaptedName != null) {
			typeReference.setName(adaptedName);
		}
		super.visitTypeReference(typeReference);
	}

	private String getAdaptedName(TypeReference typeReference) {
		String simpleName = typeReference.getSimpleName();
		String qualifier = typeReference.getQualifier();
		TypeDeclaration type = context.getTypeDeclaration(qualifier);
		boolean innerType = type != null && !context.isTypeClashingWithModule(qualifier);
		String newSimpleName = toJavaName(simpleName);
		if (innerType) {
			return getAdaptedName(new TypeReference(null, qualifier, null)) + "." + newSimpleName;
		} else {
			if (qualifier != null && !qualifier.endsWith(STRING_TYPES_INTERFACE_NAME)) {
				String newQualifier = null;
				if (qualifier != null) {
					newQualifier = Arrays.stream(qualifier.split("\\.")).map(name -> toJavaName(name, true))
							.collect(Collectors.joining("."));
				}
				return newQualifier + "." + newSimpleName;
			} else {
				return null;
			}
		}

	}
}

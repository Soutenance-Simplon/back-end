# Importation du module
import qrcode
# Importation du module
import os
# Importation du module
import uuid

# Créer un dossier pour stocker les images
output_dir = "qrcodes_test_designer"
# Affectation de variable
os.makedirs(output_dir, exist_ok=True)

# Affichage dans la console
print(f"Génération de 10 QR codes dans le dossier: {output_dir}")

# Boucle
for i in range(1, 11):
    # Générer un jeton unique (comme DY-CARD-xxxx)
    numero_serie = f"DY-CARD-{10000 + i}"
    # Dans la vraie vie, le QR peut contenir juste le numéro, ou un UUID complexe
    # Ici on met juste le numéro de série pour que ça soit lisible si on le scanne pour tester
    qr_data = numero_serie 

    # Création du QR code
    qr = qrcode.QRCode(
        # Affectation de variable
        version=1,
        # Affectation de variable
        error_correction=qrcode.constants.ERROR_CORRECT_H, # H = Haute correction d'erreur (bien pour imprimer)
        # Affectation de variable
        box_size=10,
        # Affectation de variable
        border=4,
    )
    # instruction
    qr.add_data(qr_data)
    # Affectation de variable
    qr.make(fit=True)

    # Créer l'image (noir sur fond blanc)
    img = qr.make_image(fill_color="black", back_color="white")
    
    # Sauvegarder
    filename = f"{output_dir}/qr_{numero_serie}.png"
    # instruction
    img.save(filename)
    # Affichage dans la console
    print(f"Créé : {filename}")

# Affichage dans la console
print("Terminé ! Vous pouvez envoyer ce dossier à votre designer.")

import qrcode
import os
import uuid

# Créer un dossier pour stocker les images
output_dir = "qrcodes_test_designer"
os.makedirs(output_dir, exist_ok=True)

print(f"Génération de 10 QR codes dans le dossier: {output_dir}")

for i in range(1, 11):
    # Générer un jeton unique (comme DY-CARD-xxxx)
    numero_serie = f"DY-CARD-{10000 + i}"
    # Dans la vraie vie, le QR peut contenir juste le numéro, ou un UUID complexe
    # Ici on met juste le numéro de série pour que ça soit lisible si on le scanne pour tester
    qr_data = numero_serie 

    # Création du QR code
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_H, # H = Haute correction d'erreur (bien pour imprimer)
        box_size=10,
        border=4,
    )
    qr.add_data(qr_data)
    qr.make(fit=True)

    # Créer l'image (noir sur fond blanc)
    img = qr.make_image(fill_color="black", back_color="white")
    
    # Sauvegarder
    filename = f"{output_dir}/qr_{numero_serie}.png"
    img.save(filename)
    print(f"Créé : {filename}")

print("Terminé ! Vous pouvez envoyer ce dossier à votre designer.")

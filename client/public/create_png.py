import base64

# A minimal 100x100 gray PNG
png_base64 = """
iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAGklEQVQYV2NkYGD4z4AHMP7//5/BgYGBgQ6HAgALpQP/Z0VvVAAAAABJRU5ErkJggg==
"""

png_data = base64.b64decode(png_base64.strip())
with open('E:/music player/client/public/default-cover.png', 'wb') as f:
    f.write(png_data)
print('PNG created')
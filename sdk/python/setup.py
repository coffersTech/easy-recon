from setuptools import setup, find_packages

# 直接硬编码依赖项，避免构建过程中找不到 requirements.txt 文件的问题
requirements = [
    "mysql-connector-python==8.0.30",
    "psycopg2-binary==2.9.9"
]

setup(
    name="easy-recon-sdk",
    version="1.0.0",
    packages=find_packages(),
    description="Easy Recon SDK for Python",
    author="Coffers Tech",
    author_email="contact@cofferstech.com",
    url="https://github.com/coffersTech/easy-recon",
    install_requires=requirements,
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.7',
)
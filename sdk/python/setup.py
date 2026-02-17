import os
from setuptools import setup, find_packages

# 直接硬编码依赖项，避免构建过程中找不到 requirements.txt 文件的问题
requirements = [
    "mysql-connector-python==8.0.30",
    "psycopg2-binary==2.9.9"
]

# 读取 README.md 作为长描述
with open(os.path.join(os.path.dirname(__file__), "README.md"), "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="easy-recon-sdk",
    version=os.getenv("SDK_VERSION", "1.0.1"),
    packages=find_packages(),
    description="Easy Recon SDK for Python",
    long_description=long_description,
    long_description_content_type="text/markdown",
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
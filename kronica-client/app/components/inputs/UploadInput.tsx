import { MouseEvent, useState, useCallback, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { DndContext, UniqueIdentifier } from "@dnd-kit/core";
import SortableInput from "./SortableInput";
import { FilePlus } from "lucide-react";
import { metadata } from "@/app/layout";

export type FileInput = {
    //null if this file is already on the server, non null if this file is going to be uploaded.
    data?: File;

    //to show in img tag. must always exist.
    url: string;

    //assigned based on order its added. Has NO relation to real id of data on the server.
    //used for SortableContext to identify items.
    //can't change name due to library issues.
    id: string;

    metadata: FileMetadataForm;
};

export type FileMetadataForm = {
    id?: number;
    uploadIndex?: number;
    delete?: boolean;
    caption?: string;
};

type UploadInputProps = {
    id: string;
    fileInputs: FileInput[];
    isSingleFile?: boolean;
    setFileInputs: (arg0: FileInput[]) => void;
};

type UploadThumbProps = {
    item: FileInput;
    setList: (arg0: FileInput[]) => void;
    list: FileInput[];
};

function UploadThumb({ item, list, setList }: UploadThumbProps) {
    const { data, url, metadata, id } = item;
    return (
        <div className="p-2 flex">
            {" "}
            <img className="w-10" src={url} alt="uploaded" />
            <div className="flex-col">
                {data ? (
                    <div className="flex">
                        <FilePlus />
                        New file
                    </div>
                ) : (
                    <div className="flex">Existing file</div>
                )}
                <div>File Name: {data?.name}</div>
                <div>File Size: {data && Math.round(data.size / 1000)} KB</div>
                <label>
                    Caption:{" "}
                    <input
                        value={metadata.caption}
                        onChange={(e) => {
                            const newCaption = e.target.value;

                            const l = list;
                            l.forEach((it) => {
                                if (it.id === id) {
                                    it.metadata.caption = newCaption;
                                }
                            });
                            setList(l);
                        }}
                    />
                </label>
            </div>
        </div>
    );
}

function UploadInput({
    id,
    fileInputs,
    setFileInputs,
    isSingleFile = false,
}: UploadInputProps) {
    // const [fileInputsState, setFileInputsState] = useState<FileInput[]>([]);

    // useEffect(() => {
    //   setFileInputsState(fileInputs);
    // }, [fileInputs]);

    const [increm, setIncrem] = useState(0);

    console.log(fileInputs);
    const processFile = (file: File) => {
        const reader = new FileReader();

        return new Promise((resolve, reject) => {
            reader.readAsDataURL(file);
            reader.onabort = () => {
                reject(new Error("file reading was aborted"));
            };
            reader.onerror = () => {
                reject(new Error("file reading has failed"));
            };
            reader.onload = () => {
                resolve(reader.result);
            };
        });
    };

    const onDrop = useCallback(
        async (acceptedFiles: File[]) => {
            const processedFiles = await Promise.all(
                acceptedFiles.map((acceptedFile: File) =>
                    processFile(acceptedFile)
                )
            );

            let tempIncrem = increm;
            const newFileInputs = acceptedFiles.map((acceptedFile, index) => {
                const res = {
                    data: acceptedFile,
                    url: processedFiles[index] as string,
                    id: `${tempIncrem}`,
                    metadata: {},
                };
                tempIncrem++;
                return res;
            });
            setIncrem(tempIncrem);
            setFileInputs([...fileInputs, ...newFileInputs]);
        },
        [increm, setFileInputs, fileInputs]
    );
    const { getRootProps, getInputProps } = useDropzone({ onDrop });

    // const handleDeleteClick = (e: MouseEvent<HTMLButtonElement>) => {
    //   const indexToDelete = (e.target as HTMLInputElement).value.toString();
    //   const newState = fileInputsState.filter(
    //     (inputs, i) => i !== parseInt(indexToDelete, 10)
    //   );
    //   setFileInputsState(newState);
    //   setFileInputs(newState);
    // };

    return (
        <>
            <div {...getRootProps()}>
                <input id={id} {...getInputProps()} />

                {isSingleFile ? (
                    <p>
                        [Drag and drop a file here, or click to select a file.]
                    </p>
                ) : (
                    <p>
                        [Drag and drop some files here, or click to select
                        files.]
                    </p>
                )}
            </div>
            <SortableInput<FileInput>
                list={fileInputs}
                setList={setFileInputs}
                cardComponent={UploadThumb}
            />
        </>
    );
}

export default UploadInput;

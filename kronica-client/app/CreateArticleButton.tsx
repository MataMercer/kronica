"use client";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { refreshArticles } from "./actions";
import AuthProtection from "./AuthProtection";
import MDEditor from "@uiw/react-md-editor";
import { useState } from "react";

type Inputs = {
    title: string;
    body: string;
};

export default function CreateArticleButton() {
    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
        control,
    } = useForm<Inputs>();

    const [showArticleForm, setShowArticleForm] = useState(true);

    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const formData = new FormData();
        formData.append("title", data.title);
        formData.append("body", data.body);
        const response = await fetch("http://localhost:7070/api/articles", {
            method: "POST",
            credentials: "include",
            body: JSON.stringify({
                title: data.title,
                body: data.body,
            }),
        });

        refreshArticles();
    };

    return (
        <AuthProtection>
            <Dialog>
                <DialogTrigger asChild>
                    <button className="button">CREATE ARTICLE</button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[90vw]">
                    <form onSubmit={handleSubmit(onSubmit)}>
                        <DialogHeader>
                            <DialogTitle>CREATE ARTICLE</DialogTitle>
                        </DialogHeader>
                        <div className="flex flex-col gap-4 py-4">
                            <label className="flex flex-col" htmlFor="title">
                                Title
                                <input
                                    {...register("title", { required: true })}
                                    id="title"
                                    defaultValue="Untitled"
                                />
                            </label>
                            <label className="flex flex-col" htmlFor="body">
                                Body
                                {/* <textarea
                                    {...register("body", { required: true })}
                                    id="body"
                                    defaultValue="Empty body"
                                    rows={20}
                                /> */}
                                <Controller
                                    name="body"
                                    control={control}
                                    defaultValue="Empty body"
                                    render={({ field }) => (
                                        <div data-color-mode="light">
                                            <MDEditor
                                                value={field.value}
                                                onChange={field.onChange}
                                            />
                                            {/* <MDEditor.Markdown
                                                source={field.value}
                                                style={{
                                                    whiteSpace: "pre-wrap",
                                                }}
                                            /> */}
                                        </div>
                                    )}
                                />
                            </label>

                            <button
                                className="button"
                                onClick={() => setShowArticleForm(false)}
                            >
                                Add Files
                            </button>
                        </div>
                        <DialogFooter>
                            <button className="button" type="submit">
                                Submit
                            </button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </AuthProtection>
    );
}
